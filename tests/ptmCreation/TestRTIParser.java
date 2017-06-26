package ptmCreation;

import javafx.embed.swing.SwingFXUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Jed on 18-Jun-17.
 */
public class TestRTIParser {

    @Test
    public void testCreateHSHObject(){
        try{
            RTIObjectHSH ptmObject1 = (RTIObjectHSH) RTIParser.createPtmFromFile("fishHSHBasis2.rti");
            BufferedImage renderedImage1 = SwingFXUtils.fromFXImage(ptmObject1.previewImage, null);
            ImageIO.write(renderedImage1, "png", new File("testHSHb4.png"));

            RTIObjectHSH ptmObject = (RTIObjectHSH) RTIParser.createPtmFromFile("fishyHSH.rti");
            BufferedImage renderedImage = SwingFXUtils.fromFXImage(ptmObject.previewImage, null);
            ImageIO.write(renderedImage, "png", new File("testHSHb9.png"));

            assertTrue(true);
        }catch(IOException | RTIFileException e){
            e.printStackTrace();
            fail();
        }

    }

}
