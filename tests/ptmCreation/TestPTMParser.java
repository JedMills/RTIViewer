package ptmCreation;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Jed on 18-Jun-17.
 */
public class TestPTMParser {

    @Test
    public void testCreateHSHObject(){
        try{
            PTMObjectHSH ptmObject1 = (PTMObjectHSH) PTMParser.createPtmFromFile("fishHSHBasis2.rti");
            BufferedImage renderedImage1 = SwingFXUtils.fromFXImage(ptmObject1.previewImage, null);
            ImageIO.write(renderedImage1, "png", new File("testHSHb4.png"));

            PTMObjectHSH ptmObject = (PTMObjectHSH) PTMParser.createPtmFromFile("fishyHSH.rti");
            BufferedImage renderedImage = SwingFXUtils.fromFXImage(ptmObject.previewImage, null);
            ImageIO.write(renderedImage, "png", new File("testHSHb9.png"));

            assertTrue(true);
        }catch(IOException | PTMFileException e){
            e.printStackTrace();
            fail();
        }

    }

}
