package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import exceptions.EmptyQueueException;
import implementations.MyQueue;
import implementations.MyStack;

/**
 * simple xml parser that uses the custom stack and queue to check if an xml document is constructed correctly
 */

public class XMLParser
{
	/**
	 * helper class to keep track of a start tag and the line it appears on
	 */
	private static class TagInfo
	{
		String name;
		int line;
		
		TagInfo(String name, int line)
		{
			this.name = name;
			this.line = line;
		}
	}
	
	private MyStack<TagInfo> stack;
	private MyQueue<String> errorQ;
	private MyQueue<String> extrasQ;
	
	/**
	 * creates a new parser with empty data structures
	 */
	public XMLParser()
	{
		stack = new MyStack<>();
		errorQ = new MyQueue<>();
		extrasQ = new MyQueue<>();
	}
	
	/**
	 * reads the xml file line by line and processes each tag
	 */
	
	public void parse(String fileName)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
		{
			String line;
			int lineNumber = 1;
			
			while ((line = reader.readLine()) != null)
					{
						String[] tags = extractTags(line);
						
						for (String tag : tags) 
						{
							handleTag(tag, lineNumber);
						}
						
						lineNumber++;
					}
			finalizeParsing();
			compareQueues();
			printResults();
		} 
		catch(IOException e)
		{
			System.out.println("Error reading file: "+ e.getMessage());
		}
		
	}

	/**
	 * extracts all tags from a line of xml
	 */
	private String[] extractTags(String line) 
	{
		ArrayList<String> tags = new ArrayList<>();
		
		if (line == null)
		{
			return new String[0];
		}
		
		int i = 0;
		while (i < line.length())
		{
			int start = line.indexOf('<', i);
			if (start == -1)
			{
				break;
			}
			int end = line.indexOf('>', start + 1);
			if (end == -1)
			{
				break;
			}
			String tag = line.substring(start, end + 1).trim();
			if (!tag.isEmpty())
			{
				tags.add(tag);
			}
			i = end + 1;
		}
		
		return tags.toArray(new String[0]);

	}
	
	
	/**
	 * handles a single tag using kitty's algorithm
	 */
	
	private void handleTag(String rawTag, int lineNumber) 
	{
		if (rawTag == null || rawTag.length() < 3)
		{
			return;
		}
		
		String tag = rawTag.trim();
		
		//ignore processing instructions like <?xml ... ?>
		if (tag.startsWith("<?"))
		{
			return;
		}
		
		boolean isEndTag = tag.startsWith("</");
		boolean isSelfClosing = tag.endsWith("/>");
		
		String name = extractTagName(tag);
		if (name == null || name.isEmpty())
		{
			return;
		}
		
		//ignore self closing tag
		if (isSelfClosing)
		{
			return;
		}
		
		// start tag
		if (!isEndTag)
		{
			stack.push(new TagInfo(name, lineNumber));
			return;
		}
		
		//end tag
		if (stack.isEmpty())
		{
			errorQ.enqueue("Error at line:" + lineNumber + "</" +name + "> is not consrtucted correctly.");
			return;
		}
		
		//check top of stack
		TagInfo top = stack.peek();
		if (top.name.equals(name))
		{
			stack.pop();
			return;
		}
		
		//check if matches head of errorQ
		try
		{
			if (!errorQ.isEmpty())
			{
				String head = errorQ.peek();
				if(head.contains("</" + name + ">"))
				{
					errorQ.dequeue();
					return;
				}
			}
		}
		catch(EmptyQueueException e)
		{
			//ignore
		}

		//search stack for matching start tag
		ArrayList<TagInfo> temp = new ArrayList<>();
		boolean found = false;
		
		while (!stack.isEmpty())
		{
			TagInfo popped = stack.pop();
			if (popped.name.equals(name))
			{
				found = true;
				break;
			}
			temp.add(popped);
		}
		
		if (found)
		{
			//pop each E from stack into errorQ until match
			for(TagInfo bad : temp)
			{
				errorQ.enqueue("Error at line: " + bad.line + " <" + bad.name + "> is not constructed correctly.");
			}
		}
		else
		{
			//no match in stack, so add to extrasQ
			extrasQ.enqueue("Error at line: " + lineNumber + " </" + name + ">is not constructed correctly.");
		}
		
	}

	
	/**
	 * extracts the tag name from a raw tag string and strips attributes
	 */
	private String extractTagName(String tag) 
	{
		int start = 1;
		if (tag.startsWith("</"))
		{
			start = 2;
		}
		
		int end = tag.length() - 1;
		if (tag.endsWith("/>"))
		{
			end = tag.length() - 2;
		}
		
		if (start >= end)
		{
			return null;
		}
		
		String inside = tag.substring(start, end).trim();
		if (inside.isEmpty())
		{
			return null;
		}
		
		int spaceIndex = inside.indexOf(' ');
		if (spaceIndex != -1)
		{
			inside = inside.substring(0, spaceIndex);
		}
		
		return inside;
	}
	
	/**
	 * after EOF: pop remaining stack into errorQ
	 */
	private void finalizeParsing() 
	{
		while (!stack.isEmpty())
		{
			TagInfo open = stack.pop();
			errorQ.enqueue("Error at line: " + open.line + " <" + open.name + "> is not constructed correctly.");
		}
	}
	
	/**
	 * compare errorQ and extrasQ exactly as kitty's algorithm describes
	 */

	private void compareQueues() 
	{
		boolean errorEmpty = errorQ.isEmpty();
		boolean extrasEmpty = extrasQ.isEmpty();
		
		if (errorEmpty ^ extrasEmpty)
		{
			return;
		}
		
		if (errorEmpty && extrasEmpty)
		{
			return;
		}
		
		while (!errorQ.isEmpty() && !extrasQ.isEmpty())
		{
			try
			{
				String eHead = errorQ.peek();
				String xHead = extrasQ.peek();
				
				if (!eHead.equals(xHead))
				{
					errorQ.dequeue();
				}
				else
				{
					errorQ.dequeue();
					extrasQ.dequeue();
				}
			}
			catch (EmptyQueueException e)
			{
				break;
			}
		}
	}

		/**
		 * prints the final results of the parsing
		 */
		private void printResults() 
		{
			boolean hasErrors = !errorQ.isEmpty() || !extrasQ.isEmpty();
			
			if (!hasErrors)
			{
				System.out.println("XML document is consrtucted correctly.");
				return;
			}
			
			while (!errorQ.isEmpty())
			{
				try
				{
					System.out.println(errorQ.dequeue());
				}
				catch (EmptyQueueException e)
				{
				}
				
			}
			while (!extrasQ.isEmpty())
			{
				try
				{
					System.out.println(extrasQ.dequeue());
				}
				catch (EmptyQueueException e)
				{
					
				}
			}
		}
		
		public static void main(String[] args)
		{
			if (args.length != 1)
			{
				System.out.println("Usage: java -jar Parser.jar <xmlfile>");
				return;
			}
			
			XMLParser parser = new XMLParser();
			parser.parse(args[0]);
		}
}
