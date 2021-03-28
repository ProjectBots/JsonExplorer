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
				+ "Version: 1.0.0\r\n"
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
	

	private static void rem(String arg) {
		if(arg.equals("")) return;
		
		JsonElement el = getFromPath();
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
	}


	private static void set(String arg) {
		String[] pair = arg.split(" > ");
		if(pair.length != 2) {
			System.out.println("format is wrong, use > between key and value");
			return;
		}
		String key = pair[0];
		String value = pair[1];
		
		JsonElement parent  = getFromPath();
		if(parent == null) return;
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
		JsonElement parent = getFromPath();
		if(parent == null) return;
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
				obj = JsonParser.json(sjson);
				arr = null;
			} catch (JsonParseException e) {
				try {
					arr = JsonParser.jsonArr(sjson);
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
		JsonElement parent = getFromPath();
		
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
				System.out.println("\"" + p.getAsString() + "\"");
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
				+ "cd {obj/arr}##goes into the Object/Array  "
				+ "cd###goes one back  "
				+ "  "
				+ "help###prints this  "
				+ "  "
				+ "set {key} > {value}#sets the value of a key  "
				+ "###true, false, null and numbers will be added as their type  "
				+ "###{} will add an Object, [] will add an Array  "
				+ "###ex: set hi > {}  "
				+ "###use \\s before the value to add it as String  "
				+ "###ex: set hi > \\s[]  "
				+ "  "
				+ "rem {key}##deletes this key  "
				+ "  "
				+ "save###saves the json  "
				+ "save -p###saves the file as pretty json (its just easier to read for humans)  "
				+ "save {locate}##saves the file in a specified location  "
				+ "save -p {location}#saves the file in a specified location as pretty json  "
				+ "  "
				+ "e###exits the program  ";
		
		
		System.out.println(help.replace("#", "\u0009").replace("  ", "\r\n"));
	}
	
	
	private static JsonElement getFromPath() {
		String[] paths = path.split("\\\\");
		
		JsonObject tobj = obj;
		JsonArray tarr = arr;
		
		if(paths.length > 1) {
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
					+ "type in save arguments ([-p] [{location}]) or n for no\r\n"
					+ "nothing is understood as yes");
			System.out.print("~save>");
			
			String sa = s.nextLine();
			System.out.println(sa);
			if(!sa.equals("n")) save(sa);
		}
	}
}
