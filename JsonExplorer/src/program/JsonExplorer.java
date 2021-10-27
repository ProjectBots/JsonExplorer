package program;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import include.JsonParser;
import include.NEF;

public class JsonExplorer {

	private static Scanner s = null;
	
	private static String path = "";
	private static String filePath = null;
	private static boolean isChanged = false;
	
	private static JsonObject obj = null;
	private static JsonArray arr = null;
	
	public static void main(String[] args) {
		
		System.out.println(
				  "\r\n"
				+ "\r\n"
				+ "       ██╗███████╗ ██████╗ ███╗   ██╗      ███████╗██╗  ██╗██████╗ ██╗      ██████╗ ██████╗ ███████╗██████╗ \r\n"
				+ "       ██║██╔════╝██╔═══██╗████╗  ██║      ██╔════╝╚██╗██╔╝██╔══██╗██║     ██╔═══██╗██╔══██╗██╔════╝██╔══██╗\r\n"
				+ "       ██║███████╗██║   ██║██╔██╗ ██║█████╗█████╗   ╚███╔╝ ██████╔╝██║     ██║   ██║██████╔╝█████╗  ██████╔╝\r\n"
				+ "  ██   ██║╚════██║██║   ██║██║╚██╗██║╚════╝██╔══╝   ██╔██╗ ██╔═══╝ ██║     ██║   ██║██╔══██╗██╔══╝  ██╔══██╗\r\n"
				+ "  ╚█████╔╝███████║╚██████╔╝██║ ╚████║      ███████╗██╔╝ ██╗██║     ███████╗╚██████╔╝██║  ██║███████╗██║  ██║\r\n"
				+ "   ╚════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝      ╚══════╝╚═╝  ╚═╝╚═╝     ╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝\r\n"
				+ "\r\n"
				+ "by ProjectBots\r\n"
				+ "Version: 1.1.0\r\n"
				+ "\r\n"
				+ "type help for help\r\n"
				);
		
		
		
		s = new Scanner(System.in);
		
		while(true) {
			try {
				System.out.print(path + ">");
				String in = s.nextLine();
				
				if(in.equals("e")) exit();
				
				int index = in.indexOf(" ");
				String cmd = (index == -1 ? in : in.substring(0, index));
				String arg = (index == -1 ? "" : in.substring(index+1));
				
				switch(cmd) {
				case "":
					if(!path.equals("")) show();
					break;
				case "help":
					printHelp();
					break;
				case "open":
					open(arg);
					break;
				case "cd":
					cd(arg);
					break;
				case "set":
					set(arg);
					break;
				case "rem":
					rem(arg);
					break;
				case "save":
					save(arg);
					break;
				case "search":
					search(arg);
					break;
				case "export":
					export(arg);
					break;
					
					
				default:
					System.out.println("Couldnt recognize command");
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {}
				show();
			}
		}
		
	}
	
	private static void export(String arg) {
		try {
			JsonElement je;
			
			int count = 0;
			if(arg.contains("-s ")) {
				System.out.println("please type in search arguments ([...] {term})");
				System.out.print("~search>");
				je = search(s.nextLine());
				count += 3;
			} else {
				je = getFromPath();
			}
			
			String text;
			if(arg.contains("-p ")) {
				text = JsonParser.prettyJson(je);
				count += 3;
			} else {
				text = je.toString();
			}
			arg = arg.substring(count);
			
			try {
				NEF.save(arg, text);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (BothNullException e) {
			System.out.println("Nothing to export");
		}
		
	}

	public static class Search {
		
		private boolean nested = false;
		private boolean keys = true;
		private boolean values = true;
		private boolean objects = true;
		private boolean arrays = true;
		private boolean primitives = true;
		private boolean nulls = true;
		
		private String term = null;
		
		
		
		public void setNested(boolean nested) {
			this.nested = nested;
		}

		public void setKeys(boolean keys) {
			this.keys = keys;
		}

		public void setValues(boolean values) {
			this.values = values;
		}

		public void setObjects(boolean objects) {
			this.objects = objects;
		}

		public void setArrays(boolean arrays) {
			this.arrays = arrays;
		}

		public void setPrimitives(boolean primitives) {
			this.primitives = primitives;
		}
		
		public void setNulls(boolean nulls) {
			this.nulls = nulls;
		}

		public void setTerm(String term) {
			this.term = term;
		}
		
		public static class consts {
			public static final int normal = 0;
			public static final int advanced = 1;
			public static final int regex = 2;
		}
		

		public JsonArray search(int mode, JsonElement je, String path) {
			JsonArray ret = new JsonArray();
			if(je.isJsonObject()) {
				JsonObject jo = je.getAsJsonObject();
				for(String key : jo.keySet()) {
					ret.addAll(test(mode, jo.get(key), key, path));
				}
			} else {
				JsonArray ja = je.getAsJsonArray();
				for(int i=0; i<ja.size(); i++) {
					ret.addAll(test(mode, ja.get(i), ""+i, path));
				}
			}
			return ret;
		}
		
		private JsonArray test(int mode, JsonElement e, String key, String path) {
			JsonArray ret = new JsonArray();
			if(nested && (e.isJsonArray() || e.isJsonObject())) {
				ret.addAll(search(mode, e, path + "\\" + key));
			}
			
			ifc:
			if(keys) {
				if((!primitives && e.isJsonPrimitive())
				|| (!arrays && e.isJsonArray())
				|| (!objects && e.isJsonObject())
				|| (!nulls && e.isJsonNull())
				) break ifc;
				
				switch(mode) {
				case consts.normal:
					if(!key.toLowerCase().contains(term.toLowerCase())) break ifc;
					break;
				case consts.advanced:
					if(!advMatch(key)) break ifc;
					break;
				case consts.regex:
					if(!Pattern.matches(term, key)) break ifc;
					break;
				}
				
				System.out.print(path + "> \"" + key + "\" : ");
				display(e);
				ret.add(e);
				return ret;
			}
			
			ifc:
			if(values && primitives && e.isJsonPrimitive()) {
				switch(mode) {
				case consts.normal:
					if(!e.getAsJsonPrimitive().getAsString().toLowerCase().contains(term.toLowerCase())) break ifc;
					break;
				case consts.advanced:
					if(!advMatch(e.getAsJsonPrimitive().getAsString())) break ifc;
					break;
				case consts.regex:
					if(!Pattern.matches(term, e.getAsJsonPrimitive().getAsString())) break ifc;
					break;
				}
				
				System.out.print(path + "> \"" + key + "\" : ");
				display(e);
				ret.add(e);
				return ret;
			}
			
			return ret;
		}

		private boolean advMatch(String key) {
			String[] parts = term.split("#");
			for(int i=term.length()-1; i>0; i--) {
				if(term.substring(i-1, i).equals("#")) {
					parts = add(parts, "");
				} else {
					break;
				}
			}
			
			boolean first = true;
			
			for(int i=0; i<parts.length; i++) {
				if(!parts[i].equals("")) {
					int index = key.indexOf(parts[i]);
					if(first && index != 0) return false;
					if(index == -1) return false;
					key = key.substring(index + parts[i].length());
				}
				if(i<parts.length-1 || parts[i].equals("")) {
					if(key.length() == 0) return false;
					key = key.substring(1);
				} else {
					if(key.length() != 0 && !term.endsWith("#")) return false;
				}
				first = false;
			}
			
			return true;
		}
		
		private String[] add(String[] arr, String item) {
			String[] arr2 = new String[arr.length + 1];
			System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr2[arr.length] = item;
			return arr2;
		}

		
	}
	

	private static JsonArray search(String arg) {
		
		
		int count = 0;
		Search s = new Search();
		
		if(arg.contains("-n ")) {
			s.setNested(true);
			count+=3;
		}
		if(arg.contains("-ik ")) {
			s.setValues(false);
			count+=4;
		}
		if(arg.contains("-iv ")) {
			s.setKeys(false);
			count+=4;
		}
		if(arg.contains("-eo ")) {
			s.setObjects(false);
			count+=4;
		}
		if(arg.contains("-ea ")) {
			s.setArrays(false);
			count+=4;
		}
		if(arg.contains("-ep ")) {
			s.setPrimitives(false);
			count+=4;
		}
		if(arg.contains("-en ")) {
			s.setNulls(false);
			count+=4;
		}
		
		String path = JsonExplorer.path;
		if(arg.contains("-a ")) {
			int index = path.indexOf("\\");
			path = (index != -1 ? path.substring(0, index) : path);
			s.setNested(true);
			count+=3;
		}
		
		if(arg.contains("-reg ") && arg.contains("-adv ")) {
			System.out.println("cant search advanced and regex");
			return null;
		}
		
		JsonArray ret = null;
		
		try {
			JsonElement je = getFromPath(path);
			printlns();
			if(arg.contains("-adv ")) {
				s.setTerm(arg.substring(count + 5));
				ret = s.search(Search.consts.advanced, je, path);
			} else if(arg.contains("-reg ")) {
				s.setTerm(arg.substring(count + 5));
				ret = s.search(Search.consts.regex, je, path);
			} else {
				s.setTerm(arg.substring(count));
				ret = s.search(Search.consts.normal, je, path);
			}
		} catch (BothNullException e) {
			System.out.println("nothing to search in");
		}
		
		return ret;
	}

	
	private static void rem(String arg) {
		if(arg.equals("")) return;
		
		JsonElement el;
		try {
			el = getFromPath();
		} catch (BothNullException e1) {
			System.out.println("no file is open");
			return;
		}
		if(el == null) return;
		if(el.isJsonObject()) {
			el.getAsJsonObject().remove(arg);
		} else {
			int index;
			try {
				index = Integer.parseInt(arg);
			} catch (NumberFormatException e) {
				System.out.println(arg + " is not an Index");
				return;
			}
			JsonArray arr = el.getAsJsonArray();
			if(index >= arr.size()) {
				System.out.println(arg + " is too big");
				return;
			}
			arr.remove(index);
		}
		isChanged = true;
		show();
	}


	private static void save(String arg) {
		try {
			if(path.equals("")) return;
			if(arg.equals("")) {
				NEF.save(filePath, (obj != null ? obj.toString() : arr.toString()));
				return;
			}
			
			if(arg.startsWith("-p")) {
				if(arg.endsWith("-p")) {
					if(obj != null) {
						NEF.save(filePath, JsonParser.prettyJson(obj));
					} else {
						NEF.save(filePath, JsonParser.prettyJson(arr));
					}
				} else {
					arg = arg.substring(3);
					if(obj != null) {
						NEF.save(arg, JsonParser.prettyJson(obj));
					} else {
						NEF.save(arg, JsonParser.prettyJson(arr));
					}
				}
			} else {
				NEF.save(arg, (obj != null ? obj.toString() : arr.toString()));
			}
			isChanged = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private static void set(String arg) {
		String[] pair = arg.split(" > ");
		if(pair.length != 2) {
			System.out.println("format is wrong, use > between key and value");
			return;
		}
		String key = pair[0];
		String value = pair[1];
		
		JsonElement parent;
		try {
			parent = getFromPath();
		} catch (BothNullException e) {
			System.out.println("no file is open");
			return;
		}
		if(parent.isJsonObject()) {
			parent.getAsJsonObject().add(key, string2JsonElement(value));
		} else {
			int index = Integer.parseInt(key);
			JsonArray ja = parent.getAsJsonArray();
			if(index >= ja.size()) {
				ja.add(string2JsonElement(value));
			} else {
				ja.set(index, string2JsonElement(value));
			}
			
		}
		isChanged = true;
		show();
	}
	
	private static JsonElement string2JsonElement(String arg) {
		if(arg.startsWith("\\s")) return new JsonPrimitive(arg.substring(2));
		switch(arg) {
		case "{}":
			return new JsonObject();
		case "[]":
			return new JsonArray();
		case "true":
		case "True":
		case "TRUE":
			return new JsonPrimitive(true);
		case "false":
		case "False":
		case "FALSE":
			return new JsonPrimitive(false);
		case "null":
		case "Null":
		case "NULL":
			return null;
		}
		
		if(Pattern.matches("-?\\d+(\\.\\d+)", arg)) {
			return new JsonPrimitive(Double.parseDouble(arg));
		}
		
		if(Pattern.matches("-?\\d+", arg)) {
			return new JsonPrimitive(Long.parseLong(arg));
		}
		
		return new JsonPrimitive(arg);
	}

	private static void cd(String arg) {
		if(arg.equals("")) {
			if(!path.contains("\\")) return;
			path = path.substring(0, path.lastIndexOf("\\"));
			show();
			return;
		}
		JsonElement parent;
		try {
			parent = getFromPath();
		} catch (BothNullException e1) {
			System.out.println("no file is open");
			return;
		}
		JsonElement el = null;
		if(parent.isJsonObject()) {
			el = parent.getAsJsonObject().get(arg);
		} else {
			int index;
			try {
				index = Integer.parseInt(arg);
			} catch (NumberFormatException e) {
				System.out.println(arg + " is not a valid index");
				return;
			}
			JsonArray arr = parent.getAsJsonArray();
			if(index >= arr.size()) {
				System.out.println(arg + " is too big");
				return;
			}
			el = arr.get(index);
		}
		if(el == null) {
			System.out.println(arg + " is not valid");
			return; 
		}
		if(el.isJsonArray() || el.isJsonObject()) {
			path += "\\" + arg;
			show();
		} else {
			System.out.println(arg + " is not valid");
		}
	}

	private static void open(String arg) {
		saveWarning();
		
		if(arg.equals("")) return;
		try {
			String sjson = NEF.read(arg);
			try {
				obj = JsonParser.parseObj(sjson);
				arr = null;
			} catch (JsonParseException e) {
				try {
					arr = JsonParser.parseArr(sjson);
					obj = null;
				} catch (JsonParseException e1) {
					System.out.println(arg + " is not a json");
					return;
				}
			}
			filePath = arg;
			path = arg.substring(arg.lastIndexOf("\\")+1);
			isChanged = false;
			show();
		} catch (IOException e) {
			System.out.println("couldnt open " + arg);
		}
	}

	private static void show() {
		printlns();
		JsonElement parent;
		try {
			parent = getFromPath();
		} catch (BothNullException e) {
			System.out.println("no file is open");
			return;
		}
		
		if(parent.isJsonObject()) {
			JsonObject obj = parent.getAsJsonObject();
			for(String key : obj.keySet()) {
				System.out.print("\"" + key + "\" : ");
				display(obj.get(key));
			}
		} else {
			JsonArray arr = parent.getAsJsonArray();
			for(int i=0; i<arr.size(); i++) {
				System.out.print(i + " -> ");
				display(arr.get(i));
			}
		}
		
	}
	
	private static void display(JsonElement el) {
		if(el.isJsonPrimitive()) {
			JsonPrimitive p = el.getAsJsonPrimitive();
			if(p.isNumber() || p.isBoolean()) {
				System.out.println(p.getAsString());
			} else {
				System.out.println("\"" + p.getAsString().replace("\n", "\\n") + "\"");
			}
		} else if(el.isJsonNull()) {
			System.out.println("null");
		} else if(el.isJsonArray()) {
			if(el.getAsJsonArray().size() > 0) {
				System.out.println("[...]");
			} else {
				System.out.println("[]");
			}
		} else if(el.isJsonObject()) {
			if(el.getAsJsonObject().size() > 0) {
				System.out.println("{...}");
			} else {
				System.out.println("{}");
			}
		}
	}
	

	private static void printHelp() {
		
		String help =
				  "open {file}##opens a file  "
				+ "  "
				+ "cd {obj/arr}##goes into the object/array  "
				+ "cd###goes one back  "
				+ "  "
				+ "help###prints this  "
				+ "  "
				+ "set {key} > {value}#sets the value of a key  "
				+ "###true, false, null and numbers will be added as their type  "
				+ "###{} will add an object, [] will add an array  "
				+ "###ex: set hi > {}  "
				+ "###use \\s before the value to add it as string  "
				+ "###ex: set hi > \\s[]  "
				+ "  "
				+ "rem {key}##deletes this key  "
				+ "  "
				+ "save###saves the json  "
				+ "save -p###saves the file as pretty json (its just easier to read for humans)  "
				+ "save {location}##saves the file in a specified location  "
				+ "save -p {location}#saves the file in a specified location as pretty json  "
				+ "  "
				+ "search [...] {term}#searches after a term  "
				+ "#-n##searches through nested objects/arrays from the current path  "
				+ "#-a##searches through every nested objects/arrays  "
				+ "#-ik##searches only in the keys  "
				+ "#-iv##searches only in the value  "
				+ "#-eo##exludes objects  "
				+ "#-ea##exludes arrays  "
				+ "#-ep##exludes primitives  "
				+ "#-en##exludes nulls  "
				+ "#-reg##triggers regex search (the search term is the pattern)  "
				+ "#-adv##triggers advanced search (\\# are placeholders for one or more characters)  "
				+ "###\\#itco\\# will match bitcoin but not itcoin because \\# has to stand for at least one character  "
				+ "###bit\\#coin will match bitgggcoin but not bitcoin  "
				+ "###itco will only match itco because there are no placeholders  "
				+ "  "
				+ "export [...] {location}#exports from current view to a specified location  "
				+ "#-p##exports as pretty json  "
				+ "#-s##triggers the search function, you will be asked for search arguments in the next step (see search)  "
				+ "  "
				+ "e###exits the program  ";
		
		
		System.out.println(help.replace("\\#", "{hashtag}").replace("#", "\u0009").replace("{hashtag}", "#").replace("  ", "\r\n"));
	}
	
	
	private static class BothNullException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	
	private static JsonElement getFromPath() throws BothNullException {
		return getFromPath(path);
	}
	
	
	private static JsonElement getFromPath(String path) throws BothNullException {
		String[] paths = path.split("\\\\");
		
		if(obj == null && arr == null) throw new BothNullException();
		
		JsonObject tobj = obj;
		JsonArray tarr = arr;
		
		for(int i=1; i<paths.length; i++) {
			JsonElement el = null;
			
			if(tobj != null) {
				el = tobj.get(paths[i]);
			} else {
				el = tarr.get(Integer.parseInt(paths[i]));
			}
			
			
			if(el.isJsonArray()) {
				tarr = el.getAsJsonArray();
				tobj = null;
			} else if(el.isJsonObject()) {
				tobj = el.getAsJsonObject();
				tarr = null;
			}
		}
		
		if(tobj != null) {
			return tobj;
		} else {
			return tarr;
		}
	}
	
	private static String ns = createlines();
	
	private static void printlns() {
		System.out.println(ns);
	}

	private static String createlines() {
		String n5 = "\n\n\n\n\n";
		for(int i=0; i<2; i++) {
			n5 += n5;
		}
		return n5;
	}

	private static void exit() {
		saveWarning();
		s.close();
		System.exit(0);
	}
	
	private static void saveWarning() {
		if(isChanged) {
			System.out.println("Do you want to save first?\r\n"
					+ "please type in save arguments ([-p] [{location}]) or n for no\r\n"
					+ "nothing is understood as yes");
			System.out.print("~save>");
			
			String sa = s.nextLine();
			System.out.println(sa);
			if(!sa.equals("n")) save(sa);
		}
	}
}
