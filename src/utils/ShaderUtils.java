package utils;

import java.io.*;

/**
 * Created by jed on 23/05/17.
 */
public class ShaderUtils {

    public static String readFromFile(String name)
    {
        StringBuilder source = new StringBuilder();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(name)));

            String line;
            while ((line = reader.readLine()) != null)
            {
                source.append(line).append("\n");
            }

            reader.close();
        }
        catch (Exception e)
        {
            System.err.println("Error loading source code: " + name);
            e.printStackTrace();
        }

        return source.toString();
    }

}
