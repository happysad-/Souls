package netwr.souls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils
{
	public static String loadResource(String path)
	{
		System.out.println("Attempting to load: " + path);
		String result = "";
		
        try
        {
        	File in = new File(path);
        	
        	if(in.exists())
        	{
	        	Scanner scanner = new Scanner(in, "UTF-8");
	        	result = scanner.useDelimiter("\\A").next();
	        	scanner.close();
        	}
        	else
        		System.err.println("Resource at: " + path + "does not exist!");
        	
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return null;
        }
        
        return result;
	}
	
	public static List<String> readAllLines(String fileName)
	{
        List<String> list = new ArrayList<String>();
        
        try
        {
        	File in = new File(fileName);
        	BufferedReader reader = new BufferedReader(new FileReader(in));
        	
        	while(reader.ready())
        	{
        		list.add(reader.readLine());
        	}
        	
        	reader.close();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        return list;
    }

    public static float[] listToArray(List<Float> list)
    {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        
        for (int i = 0; i < size; i++)
        {
            floatArr[i] = list.get(i);
        }
        
        return floatArr;
    }
    
    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

    public static boolean existsResourceFile(String fileName) {
        boolean result;
        try (InputStream is = Utils.class.getResourceAsStream(fileName ) ) {
            result = is != null;
        } catch (Exception excp) {
            result = false;
        }
        return result;
}
}
