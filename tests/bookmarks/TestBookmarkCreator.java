package bookmarks;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jed on 25-Jun-17.
 */
public class TestBookmarkCreator {

    @Test
    public void testReadBookmarks(){
        String fileName = "lightbulb_ptm.xmp";

        try {
            ArrayList<Bookmark> bookmarks = readBookmarks(fileName);

            assertTrue(true);
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }


    public static ArrayList<Bookmark> readBookmarks(String fileName) throws Exception{
        File file = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        NodeList nodeList = doc.getElementsByTagName("Bookmark");
        System.out.println("----------------------------------");

        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);

            System.out.println("Current element: " + node.getNodeName());

            if(node.getNodeType() == Node.ELEMENT_NODE){

            }
        }

        return new ArrayList<Bookmark>();
    }
}
