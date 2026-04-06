package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import exceptions.EmptyQueueException;
import implementations.MyQueue;
import implementations.MyStack;

/**
 * XMLParser is the main entry point for the XML validation tool developed for
 * CPRG 304 Assignment 2 at SAIT Polytechnic.
 *
 * <p>This parser reads an XML file supplied via the command line, tokenizes
 * each line into individual tag strings, and applies Kitty's XML Parser
 * Algorithm to identify structural errors. Three custom data structures drive
 * the algorithm:</p>
 * <ul>
 *   <li>{@code stack}   – holds open start-tags in LIFO order as they are
 *       encountered in the document.</li>
 *   <li>{@code errorQ}  – accumulates start-tags that could not be matched to
 *       a closing tag, reported at the end.</li>
 *   <li>{@code extrasQ} – holds end-tags that had no corresponding start-tag
 *       anywhere in the stack.</li>
 * </ul>
 *
 * <p><b>Usage:</b> {@code java -jar Parser.jar <path-to-xml-file>}</p>
 *
 * @author Clark James Gift Etis (parsing algorithm),
 *         Sagar Girishbhai Kumbhar (output formatting, Javadoc, QA)
 * @version 1.0
 */
public class XMLParser
{
    // -----------------------------------------------------------------------
    // Inner helper class
    // -----------------------------------------------------------------------

    /**
     * Lightweight container that pairs a tag name with the source line on
     * which it appeared.  Stored on the stack so that error messages can
     * include the exact line number of every unclosed start-tag.
     */
    private static class TagInfo
    {
        /** The bare tag name (e.g. {@code "Language"}, {@code "Driver"}). */
        String name;

        /** 1-based line number where this tag was found in the source file. */
        int line;

        /**
         * Constructs a {@code TagInfo} record.
         *
         * @param name the tag name extracted from the raw token.
         * @param line the 1-based source line number.
         */
        TagInfo( String name, int line )
        {
            this.name = name;
            this.line = line;
        }
    }

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------

    /** Stack of open start-tags encountered so far; LIFO ordering. */
    private MyStack<TagInfo> stack;

    /**
     * Queue of error messages for start-tags that were never properly closed,
     * or end-tags that were encountered when the stack was empty.
     */
    private MyQueue<String> errorQ;

    /**
     * Queue of error messages for end-tags that had no matching start-tag
     * anywhere in the stack at the time they were encountered.
     */
    private MyQueue<String> extrasQ;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a new {@code XMLParser} with empty stack and queues, ready
     * to parse a fresh document.
     */
    public XMLParser()
    {
        stack   = new MyStack<>();
        errorQ  = new MyQueue<>();
        extrasQ = new MyQueue<>();
    }

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /**
     * Reads the XML file at {@code fileName} line by line, extracts every tag
     * token from each line, and processes them through Kitty's algorithm.
     *
     * <p>After the entire file has been consumed, the method calls
     * {@link #finalizeParsing()} to flush any remaining open tags, then
     * {@link #compareQueues()} to reconcile {@code errorQ} against
     * {@code extrasQ}, and finally {@link #printResults()} to emit the
     * formatted output.</p>
     *
     * @param fileName path to the XML file to validate.
     */
    public void parse( String fileName )
    {
        try( BufferedReader reader = new BufferedReader( new FileReader( fileName ) ) )
        {
            String line;
            int lineNumber = 1; // 1-based counter that increments after each line

            // Read the file one line at a time
            while( ( line = reader.readLine() ) != null )
            {
                // Pull every tag token from this line
                String[] tags = extractTags( line );

                // Process each token through Kitty's algorithm
                for( String tag : tags )
                {
                    handleTag( tag, lineNumber );
                }

                lineNumber++;
            }

            // Flush any start-tags still on the stack into errorQ
            finalizeParsing();

            // Reconcile the two error queues per Kitty's algorithm
            compareQueues();

            // Print the final validated output
            printResults();
        }
        catch( IOException e )
        {
            System.out.println( "Error reading file: " + e.getMessage() );
        }
    }

    // -----------------------------------------------------------------------
    // Private parsing methods
    // -----------------------------------------------------------------------

    /**
     * Scans a single line of raw XML text and extracts every {@code <...>}
     * tag token it contains.
     *
     * <p>Each token includes its angle brackets so that callers can reliably
     * distinguish start-tags, end-tags, and self-closing tags.</p>
     *
     * @param line a single line read from the XML file; may be {@code null}.
     * @return an array of tag-token strings found on this line; empty array if
     *         none are found or {@code line} is {@code null}.
     */
    private String[] extractTags( String line )
    {
        ArrayList<String> tags = new ArrayList<>();

        // Guard against null lines (e.g. empty files)
        if( line == null )
        {
            return new String[0];
        }

        int i = 0;
        while( i < line.length() )
        {
            // Locate the opening angle bracket
            int start = line.indexOf( '<', i );
            if( start == -1 )
            {
                break; // No more tags on this line
            }

            // Locate the matching closing angle bracket
            int end = line.indexOf( '>', start + 1 );
            if( end == -1 )
            {
                break; // Malformed tag — skip the rest of the line
            }

            // Extract and trim the tag token
            String tag = line.substring( start, end + 1 ).trim();
            if( !tag.isEmpty() )
            {
                tags.add( tag );
            }

            // Advance past this tag for the next iteration
            i = end + 1;
        }

        return tags.toArray( new String[0] );
    }

    /**
     * Processes a single tag token according to Kitty's XML Parser Algorithm.
     *
     * <p>The algorithm distinguishes three tag categories:</p>
     * <ol>
     *   <li><b>Processing instructions</b> (e.g. {@code <?xml?>}) – skipped.</li>
     *   <li><b>Self-closing tags</b> (e.g. {@code <br/>}) – skipped; they
     *       need no matching end-tag.</li>
     *   <li><b>Start-tags</b> (e.g. {@code <div>}) – pushed onto the
     *       stack.</li>
     *   <li><b>End-tags</b> (e.g. {@code </div>}) – compared against the
     *       stack head:
     *       <ul>
     *         <li>If the stack is empty the end-tag goes to {@code errorQ}.</li>
     *         <li>If it matches the head, both are consumed (pop).</li>
     *         <li>Otherwise the stack is searched for a matching start-tag;
     *             skipped entries go to {@code errorQ}, and if no match is
     *             found the end-tag goes to {@code extrasQ}.</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param rawTag    the full tag token including angle brackets.
     * @param lineNumber the 1-based source line number; used in error messages.
     */
    private void handleTag( String rawTag, int lineNumber )
    {
        // Ignore null or trivially short tokens
        if( rawTag == null || rawTag.length() < 3 )
        {
            return;
        }

        String tag = rawTag.trim();

        // Skip XML processing instructions (e.g. <?xml version="1.0"?>)
        if( tag.startsWith( "<?" ) )
        {
            return;
        }

        boolean isEndTag     = tag.startsWith( "</" );
        boolean isSelfClosing = tag.endsWith( "/>" );

        // Extract just the tag name for comparisons
        String name = extractTagName( tag );
        if( name == null || name.isEmpty() )
        {
            return;
        }

        // Self-closing tags require no stack interaction
        if( isSelfClosing )
        {
            return;
        }

        // ---- Start-tag: push onto the stack ----
        if( !isEndTag )
        {
            stack.push( new TagInfo( name, lineNumber ) );
            return;
        }

        // ---- End-tag handling ----

        // Case 1: Stack is empty — nowhere to match, record in errorQ
        if( stack.isEmpty() )
        {
            errorQ.enqueue( "Error at line " + lineNumber
                    + ": </" + name + "> has no matching opening tag." );
            return;
        }

        // Case 2: End-tag matches the top of the stack — perfect match, pop it
        TagInfo top = stack.peek();
        if( top.name.equalsIgnoreCase( name ) )
        {
            stack.pop();
            return;
        }

        // Case 3: Mismatch — check errorQ head first (Kitty's algorithm step)
        try
        {
            if( !errorQ.isEmpty() )
            {
                String head = errorQ.peek();
                // If the errorQ head already mentions this end-tag, consume it
                if( head.contains( "</" + name + ">" ) )
                {
                    errorQ.dequeue();
                    return;
                }
            }
        }
        catch( EmptyQueueException e )
        {
            // Queue was empty — nothing to match against
        }

        // Case 4: Search deeper in the stack for a matching start-tag
        ArrayList<TagInfo> temp = new ArrayList<>();
        boolean found = false;

        while( !stack.isEmpty() )
        {
            TagInfo popped = stack.pop();
            if( popped.name.equalsIgnoreCase( name ) )
            {
                // Match found deeper in the stack
                found = true;
                break;
            }
            // Any tags skipped over are structural errors
            temp.add( popped );
        }

        if( found )
        {
            // Every start-tag skipped during the search is an error
            for( TagInfo bad : temp )
            {
                errorQ.enqueue( "Error at line " + bad.line
                        + ": <" + bad.name + "> is missing its closing tag." );
            }
        }
        else
        {
            // End-tag had no matching start-tag anywhere in the stack
            extrasQ.enqueue( "Error at line " + lineNumber
                    + ": </" + name + "> is an unexpected closing tag." );

            // Restore all tags we popped while searching
            for( int j = temp.size() - 1; j >= 0; j-- )
            {
                stack.push( temp.get( j ) );
            }
        }
    }

    /**
     * Extracts the bare tag name from a raw tag token by stripping angle
     * brackets, leading slashes, trailing slashes, and any attributes.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code <Language name="x">} → {@code Language}</li>
     *   <li>{@code </Driver>}           → {@code Driver}</li>
     *   <li>{@code <br/>}               → {@code br}</li>
     * </ul>
     *
     * @param tag the raw tag token including angle brackets.
     * @return the bare tag name, or {@code null} if the token is malformed.
     */
    private String extractTagName( String tag )
    {
        // Determine where the name content starts
        int start = 1;
        if( tag.startsWith( "</" ) )
        {
            start = 2; // Skip past the slash in end-tags
        }

        // Determine where the name content ends (before '>' or '/>')
        int end = tag.length() - 1;
        if( tag.endsWith( "/>" ) )
        {
            end = tag.length() - 2;
        }

        if( start >= end )
        {
            return null; // Malformed token
        }

        String inside = tag.substring( start, end ).trim();
        if( inside.isEmpty() )
        {
            return null;
        }

        // Strip any attributes — keep only the first whitespace-delimited token
        int spaceIndex = inside.indexOf( ' ' );
        if( spaceIndex != -1 )
        {
            inside = inside.substring( 0, spaceIndex );
        }

        return inside;
    }

    /**
     * Flushes any start-tags still remaining on the stack into {@code errorQ}
     * after the entire file has been read.
     *
     * <p>A start-tag left on the stack means the document ended before its
     * corresponding end-tag was encountered.</p>
     */
    private void finalizeParsing()
    {
        // Every remaining start-tag on the stack is an unclosed tag error
        while( !stack.isEmpty() )
        {
            TagInfo open = stack.pop();
            errorQ.enqueue( "Error at line " + open.line
                    + ": <" + open.name + "> is missing its closing tag." );
        }
    }

    /**
     * Reconciles {@code errorQ} against {@code extrasQ} as specified by
     * Kitty's XML Parser Algorithm.
     *
     * <p>When both queues are non-empty, their heads are compared pairwise.
     * If the heads differ, the {@code errorQ} head is consumed (the mismatch
     * was already captured). If they are identical, both are consumed together.
     * The loop continues until at least one queue is empty.</p>
     */
    private void compareQueues()
    {
        boolean errorEmpty  = errorQ.isEmpty();
        boolean extrasEmpty = extrasQ.isEmpty();

        // If exactly one queue is non-empty, nothing to reconcile
        if( errorEmpty ^ extrasEmpty )
        {
            return;
        }

        // If both are empty, there are no errors at all
        if( errorEmpty && extrasEmpty )
        {
            return;
        }

        // Compare heads of both queues until one is exhausted
        while( !errorQ.isEmpty() && !extrasQ.isEmpty() )
        {
            try
            {
                String eHead = errorQ.peek();
                String xHead = extrasQ.peek();

                if( !eHead.equals( xHead ) )
                {
                    // Heads differ — consume only the errorQ head
                    errorQ.dequeue();
                }
                else
                {
                    // Heads are identical — consume both
                    errorQ.dequeue();
                    extrasQ.dequeue();
                }
            }
            catch( EmptyQueueException e )
            {
                // One queue was exhausted mid-loop — stop reconciliation
                break;
            }
        }
    }

    /**
     * Prints the final parsing results to standard output.
     *
     * <p>If neither {@code errorQ} nor {@code extrasQ} contains any messages,
     * a single confirmation line is printed. Otherwise, every message in both
     * queues is printed in order, with a header and footer separator.</p>
     */
    private void printResults()
    {
        boolean hasErrors = !errorQ.isEmpty() || !extrasQ.isEmpty();

        if( !hasErrors )
        {
            // Document is well-formed — confirm with a clear success message
            System.out.println( "XML document is well-formed. No errors detected." );
            return;
        }

        // Print a clearly formatted error report
        System.out.println( "XML Parsing Error Report" );
        System.out.println( "============================================================" );

        // Drain errorQ — unclosed or mismatched start-tags
        while( !errorQ.isEmpty() )
        {
            try
            {
                System.out.println( errorQ.dequeue() );
            }
            catch( EmptyQueueException e )
            {
                // Queue exhausted — stop
                break;
            }
        }

        // Drain extrasQ — unexpected end-tags with no matching start-tag
        while( !extrasQ.isEmpty() )
        {
            try
            {
                System.out.println( extrasQ.dequeue() );
            }
            catch( EmptyQueueException e )
            {
                // Queue exhausted — stop
                break;
            }
        }

        System.out.println( "============================================================" );
    }

    // -----------------------------------------------------------------------
    // Main method
    // -----------------------------------------------------------------------

    /**
     * Application entry point.
     *
     * <p>Expects exactly one command-line argument: the path to the XML file
     * to validate. Prints usage instructions and exits if the argument is
     * missing.</p>
     *
     * @param args command-line arguments; {@code args[0]} must be the XML file
     *             path.
     */
    public static void main( String[] args )
    {
        if( args.length != 1 )
        {
            System.out.println( "Usage: java -jar Parser.jar <xmlfile>" );
            return;
        }

        // Instantiate the parser and run it against the provided file
        XMLParser parser = new XMLParser();
        parser.parse( args[0] );
    }
}
