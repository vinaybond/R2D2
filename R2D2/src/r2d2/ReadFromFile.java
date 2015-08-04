package r2d2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("all")
public class ReadFromFile {

	//Example
	@Test(dataProviderClass=ReadFromFile.class,dataProvider="readFromXLS")
	@MyDataProvider("config/a2osanity.xls,createdeal,false")
	public void xlsDP(String a, String b, String c, String d, String e, String f, String g){
		System.out.println("a:"+ a +" | "+ b +" | "+c +" | "+d +" | "+e +" | "+f +" | "+g);
	}

	//Example
	@Test(dataProviderClass=ReadFromFile.class,dataProvider="readFromCSV")
	@MyDataProvider("config/my.csv,false")
	public void csvDP(String a, String b, String c){
		System.out.println("a:"+ a +" | "+ b +" | "+c );
	}
	
	
	
	/* Read from CSV 
	 * 
	 */
	@DataProvider(name = "readDataFromCSV")
	public static String[][] readFromCSV(Method testMethod){
		String[] param = (testMethod.getAnnotation(MyDataProvider.class).value()).split(",");
		
		String csvFileName = param[0];
		boolean skipTitleRow = false ;// = param[1].trim().equalsIgnoreCase("true")? true: false;

		if (param.length == 2){
			skipTitleRow = param[1].trim().equalsIgnoreCase("true") ? true: false;
		} 

		
		String[][] array = readFromCSV(csvFileName, skipTitleRow);
		return array;
	}

	
	public static String[][] readFromCSV(String csvFileName, boolean skipTitleRow){
		List<String> cmdList = new ArrayList<String>();
		int count=0;
		int fieldCount=0;
		
		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(csvFileName));
			String line="";

			while( (line = br.readLine()) != null){
				if (skipTitleRow){
					skipTitleRow=false;
					continue;
				}

				cmdList.add(line);
				count = StringUtils.countMatches(line, ",");
				fieldCount = (count > fieldCount ? count : fieldCount);
			}
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		count=0;
		
		String[][] arr = new String[cmdList.size()][];
		int i=0;
		String line=null;
		ListIterator<String> itr = cmdList.listIterator();
		
		
		while (itr.hasNext()){
			line = (String) itr.next();
			count = StringUtils.countMatches(line, ",");

			if (count < fieldCount ){
				for(int j=1; j <= (fieldCount - count);j++){
					line=line+", "; // in order to make number of field consistent, add "," separator
				}
			}
			
			
			arr[i]=  line.split(",");
			i++;
			System.out.println("LINE: "+line);

		}

		return arr;
	}
	


	/*
	 *  Read from XLS File and return as 2D array
	 */
	@DataProvider(name = "readFromXLS")
    public static String[][] readFromXLS(Method testMethod){
		String[] param = (testMethod.getAnnotation(MyDataProvider.class).value()).split(",");
		
		String xlsFileName = param[0];
		String sheetName = (param.length > 1) ? param[1] : "Sheet1";
		boolean skipTitleRow = false;
		if (param.length == 3){
			skipTitleRow = param[2].trim().equalsIgnoreCase("true")? true: false;
		} 
		
		String array[][] = readFromXLS(xlsFileName, sheetName, skipTitleRow);
		return array;
    }
	
	
    public static String[][] readFromXLS(String xlsFileName, String sheetName, Boolean skipTitleRow){
		
        File inputWorkbook = new File(xlsFileName);
        Workbook w;

        String[][] data=null;
		try{
            w = Workbook.getWorkbook(inputWorkbook);
            // Get the first sheet if sheet name is not specified in parameters separated by ","
            Sheet sheet = (sheetName.trim().matches("")) ?  w.getSheet(0) : w.getSheet(sheetName); 

            // Throw exception is sheet with given name is not found.
            if (sheet == null){
            	throw new NullPointerException("\nSheet " + sheetName +" Does not exists in : "+xlsFileName);
            }
            
            //System.out.println("Sheet: " + sheet);
            data = new String[sheet.getRows()][sheet.getColumns()];

            //System.out.println(sheet.getColumns() +  " " +sheet.getRows());
            
            // in case of three parameter pass, check if third parameter indicates if sheet has title as first row
            int skip=0;
    		for (int i=0; i < sheet.getRows() ; i++){
    			if (skipTitleRow){
    				skipTitleRow = false;
    				skip=1;
    				continue;
    			}
    			
            	for (int j = 0; j < sheet.getColumns(); j++){
                	data[i-skip][j] = sheet.getCell(j,i).getContents();
                }
            	//System.out.println(" ");
            }
        		
        	//resize the array in case comments are there which reduces the actuals records
            data = (String[][]) Arrays.copyOf(data,sheet.getRows());
            
        }catch (Exception e){
        	e.printStackTrace();
        }

		return data;
    }
    


	
	

	
	/*
	 *  Read data from JSON file/XLS-JSON  and return JSON Array
	 */
    public static Object[][] readFromJsonFile(String jsonFileName) throws Exception{
		
		BufferedReader br = new BufferedReader(new FileReader(jsonFileName));
		
		String sCurrentLine="";
		String str="";		
		while (( sCurrentLine = br.readLine()) != null) {
			str += sCurrentLine;
		}
		
		//read file and create json array out of it
		JSONArray jsonArray = new JSONArray(str);
		
		//specify lenght of array for objects
		Object[][] ret=new Object[jsonArray.length()][];

		
        JSONObject obj = null;
		for(int i =0 ; i< jsonArray.length(); i++){
	        obj = new JSONObject(jsonArray.get(i).toString());
			ret[i]	= new Object[] {obj}; // push object to array
		}
		br.close();
		return ret;
		
	}
    
	
	
	//Read from XLS and covert data to JSON and send JSON array
	
    @DataProvider(name = "readFromJSON")
    public static Object[][] readFromJSON(Method testMethod) throws Exception{
		String[] param = (testMethod.getAnnotation(MyDataProvider.class).value()).split(",");
		Object[][] ret = null;
		if (param[0].toLowerCase().matches(".*\\.xls.*")) {
			String xlsFileName = param[0];
			String sheetName = param[1];
			String uniqueKey = param[2];
			ret = xlsToJson(xlsFileName, sheetName,uniqueKey );
		}else {
			String jsonFileName = param[0];
			ret = readFromJsonFile(jsonFileName);
		}

		return ret;
		
	}
	
	

    public static Object[][] xlsToJson(String xlsFileName, String sheetName, String uniqueKey ) throws Exception{
		Pattern pattern = null;
		Matcher matcher = null;
		
		String key="";
		String value="";
		String[][] xlsDataArray =ReadFromFile.readFromXLS(xlsFileName, sheetName, false);

		Map<String, List<?>> hm = new HashMap<String, List<?>>();	//Hashmap stores JSONArray name and key of the element 
		List<String> elements = new ArrayList<String>();	// List stores the keys of JSON element

		JSONObject mainObj = new JSONObject();	//holds the data for one Object
		
		List<JSONObject> finalJsonList = new ArrayList<JSONObject>(); //hold the  list of Objects. i.e. list of mainObj
		int finalJsonObjectCount=-1;
		
		
		
		//Read the column headers to form a JSON structure
		for (int i=0; i< xlsDataArray[0].length;i++){
			String cellValue = xlsDataArray[0][i];
			
			pattern =  Pattern.compile("(.*)(\\.)(.*)");
			matcher = pattern.matcher(cellValue);

			
			if (matcher.find()){ // if JSON Array exist then add JSON key in the structure to it hashmap
		    	key=matcher.group(1);
		    	value=matcher.group(3);
				if (hm.containsKey(key)){
					List values = (List)hm.get(key);
					values.add(value);
				}else{
					List values = new ArrayList();
					values.add(i);	//column number when JSONArray starts in xls file 
					values.add(value);
					hm.put(key, values);
				}
				
		    }else{	//if JSONArray does not exist it means it is regular element which is to be store in list
		    	elements.add(i+":"+cellValue);
		    }

		}
		
		
		
		/*
		 * JSON Object creation start from here 
		 * Iterate through xls data array and populate main JSON object
		 */

		List<JSONArray> jaList = new ArrayList<JSONArray>();
		

		// Iterate through each record in XLS data object
		for (int rowCnt=1; rowCnt < xlsDataArray.length;rowCnt++){
			
			//Check if new Row is not duplicate by matching it with unique key
			List<String> uniqueList = new ArrayList<String>(Arrays.asList(uniqueKey.split("\\|")));
			try {
				for(String ele: elements){
					for(String tmpKey: uniqueList){
						if (ele.matches(".*"+tmpKey+".*")){
							pattern =  Pattern.compile("(.*)(:)(.*)");
							matcher = pattern.matcher(ele);
							if (matcher.find()){	
								int index = Integer.parseInt(matcher.group(1));
								String s = xlsDataArray[rowCnt][index];
								
								//if cell value from current row exist in mainObj then remove that element from uniqueList
								if (mainObj.get(tmpKey).toString().matches(s)){	 
									uniqueList.remove(tmpKey);
									break;
								}
							}
						}
					}
					if(uniqueList.size() == 0){	//Empty element indicates repetead Unique key, which does not creates a new JSSon object
						//System.out.println("All Elements Repeated");
						break;
					}
				}
			} catch (Exception e) { 
				//System.out.println("E: New Array"+niv);
			}

			//non-Empty element indicates current row is not duplicate, Time to create new JSON object
			if(uniqueList.size() != 0){ 
				//System.out.println("New Array: ");
				finalJsonObjectCount++;
				finalJsonList.add(finalJsonObjectCount,new JSONObject());
				jaList = new ArrayList<JSONArray>();
				mainObj= new JSONObject();
				//break;
			}

				
			//Iterate through the elements which are not array but regular key:value element
			for (int i = 0; i < elements.size(); i++) {
				String currEle = elements.get(i);
				pattern =  Pattern.compile("(.*)(:)(.*)");
				matcher = pattern.matcher(currEle);
				
				if (matcher.find()){	
					key = matcher.group(3);
					int index = Integer.parseInt(matcher.group(1));
					if(mainObj.has(key)){
						
						break;
					}
					
					value = xlsDataArray[rowCnt][index];
					mainObj.put(key,value);
				}
			}

			
			
			// Iterate through XLS-JSON Arrays
			int jsonArryaIndex = 0;

			for (String arrayKey : hm.keySet()) {
				JSONObject jo = new JSONObject();

				for (int arrayCnt=1;arrayCnt<hm.get(arrayKey).size();arrayCnt++){
				    int index =(Integer) hm.get(arrayKey).get(0)+arrayCnt-1; 
			    	key= hm.get(arrayKey).get(arrayCnt).toString();
			    	value = xlsDataArray[rowCnt][index];
			    	jo.put(key, value);
			    }

			    // Add array to existing array list else create a new arrayList to given index
			    try{
			    	jaList.get(jsonArryaIndex).put(jo);
			    }catch(IndexOutOfBoundsException e){
					jaList.add(jsonArryaIndex, new JSONArray().put(jo));
			    }
			    
			    mainObj.put(arrayKey, jaList.get(jsonArryaIndex));
			    jsonArryaIndex++;
			    //Copy current Object to new JSONArrayElement
			    finalJsonList.remove(finalJsonObjectCount);
				finalJsonList.add(finalJsonObjectCount,new JSONObject(mainObj.toString()));
			}
			
		}
		//System.out.println("JSON"+finalJsonList);	
		Object[][] ret=new Object[finalJsonList.size()][];

		Object obj = null;
		for(int i =0 ; i< finalJsonList.size(); i++){
			obj = new JSONObject(finalJsonList.get(i).toString());
			ret[i]	= new Object[] {obj}; // push object to array
		}

		return ret;

	
	}


    
    
    
	
	
}
	
	
	
//}
