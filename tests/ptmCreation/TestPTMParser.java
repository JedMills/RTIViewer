package ptmCreation;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.junit.Test;

import javax.imageio.ImageIO;
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
            PTMObjectHSH ptmObject = (PTMObjectHSH) PTMParser.createPtmFromFile("fishFossil.rti");
            assertEquals(ptmObject.getWidth(), 1574);
            assertEquals(ptmObject.getHeight(), 1220);

            //RenderedImage renderedImage = SwingFXUtils.fromFXImage(ptmObject.createNormalMap(), null);
            //ImageIO.write(renderedImage, "png", new File("testHSH.png"));
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(ptmObject.previewImage, null);
            ImageIO.write(renderedImage, "png", new File("testHSH.png"));

            assertTrue(true);
        }catch(IOException | PTMFileException e){
            e.printStackTrace();
            fail();
        }

    }

}
