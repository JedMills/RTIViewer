package ptmCreation;

import java.io.*;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import utils.Utils;


/**
 * This class is responsible for parsing pm files and creating PTMObjects from the data. The parser accepts
 * PTM version 1.2, and the following file formats:
 *      PTM_FORMAT_RGB
 *
 *
 *
 * Created by jed on 14/05/17.
 */
public class PTMParser {

    /**Used for checking the PTM version fo the file used*/
    private static final String[] acceptedVersions = new String[]{"PTM_1.2"};

    /**Used for checking the PTM format of the passed file*/
    private static final String[] acceptedFormats = new String[]{"PTM_FORMAT_RGB"};

    /**Scaling coefficients used for RGB format found in file header*/
    private static float[] scaleCoeffs;

    /**Bias coefficients for RGB format found in file header*/
    private static int[] biasCoeffs;


    /**
     * Reads the .ptm file passed in the fileName argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new ptmCreation.PTMObject and returns that.
     *
     * @param fileName              the path to the .ptm file
     * @return                      a new ptmCreation.PTMObject using the data in he .ptm file
     * @throws IOException          if there's an error when trying to access the file
     * @throws PTMFileException     if there's an error in file type/format/parsing the file
     */
    public static PTMObject createPtmFromFile(String fileName) throws IOException, PTMFileException{
        //check the version and format of the file, and get the file format
        String format = getFileFormat(fileName);

        //get the PTM version, file format, width, height, and coefficients
        int[] headerData = getHeaderData(fileName, format);

        //get the coefficients for the individual texels
        int[][][] texelData = getTexelData(fileName, format, headerData[0], headerData[1], headerData[2]);

        //create the ptmCreation.PTMObject from the data
        return new PTMObject(fileName, headerData[1], headerData[2], texelData);
    }


    /**
     * Reads the .ptm file passed in the file argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new ptmCreation.PTMObject and returns that.
     *
     * @param file                  the file to create the PTM object from
     * @return                      a new ptmCreation.PTMObject using the data in he .ptmfile
     * @throws IOException          if there's an error when trying to access the file
     * @throws PTMFileException     if there's an error in file type/format/parsing the file
     */
    public static PTMObject createPtmFromFile(File file) throws IOException, PTMFileException{
        return createPtmFromFile(file.getAbsolutePath());
    }


    /**
     * Checks that the PTM version and format type are in the accepted lists (see attributes), and throws
     * a ptmCreation.PTMFileException if they aren't. Returns the file format if everything ok.
     *
     * @param fileName              path to the .ptm file
     * @return                      the file format,sound on line 2 in a ptm file
     * @throws IOException          if there's an error trying to access the file
     * @throws PTMFileException     if the PTM version or format are not in the accepted types
     */
    private static String getFileFormat(String fileName) throws IOException, PTMFileException{
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        //there should be a PTM version declaration on line 1, check it's 1.2
        if(!Utils.checkIn(reader.readLine(), acceptedVersions)){
            throw new PTMFileException("File does not contain accepted version on line 1");
        }

        //the next line has the file format
        String fileFormat = reader.readLine();
        reader.close();

        //check the format's an accepted version
        if(!Utils.checkIn(fileFormat, acceptedFormats)){
            throw new PTMFileException("File does nor contain accepted format on line 2");
        }

        return fileFormat;
    }


    /**
     * Parses the header of the PTM file as appropriate to the file type. Returns an array containing
     * dataStartPos (start position of the texel data for reading), width (width of image), height
     * (height of image). Throws a ptmCreation.PTMFileException if there's some parsing error.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @return                      an array containing {dataStartPosition, width, height}
     * @throws IOException          if there's an error trying to access the file
     * @throws PTMFileException     if there's an error parsing the .ptm file
     */
    private static int[] getHeaderData(String fileName, String format) throws IOException, PTMFileException{
        //for the PTM_FORMAT_RGB file type
        if(format.equals("PTM_FORMAT_RGB")) {
            //make a read to read in the header section of the .ptm file
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            //read the first 6 lines fo the file for the header
            String header = "";
            for (int i = 0; i < 6; i++) {
                header += reader.readLine() + " ";
            }
            int dataStartPos = header.length();
            reader.close();

            //split the header into each item in it
            String[] items = header.split("(\\s+)|(\\n+)");

            //get RTI type, file type, image width, image height
            int width, height;
            try {
                width = Integer.parseInt(items[2]);
                height = Integer.parseInt(items[3]);
            } catch (NumberFormatException e) {
                throw new PTMFileException("Error parsing the width and height from file");
            }

            //get the 6 scaling coefficients
            scaleCoeffs = new float[6];
            for (int i = 0; i < 6; i++) {
                scaleCoeffs[i] = Float.parseFloat(items[i + 4]);
            }

            //get the six bias coefficients
            biasCoeffs = new int[6];
            for (int i = 0; i < 6; i++) {
                biasCoeffs[i] = Integer.parseInt(items[i + 10]);
            }
            return new int[]{dataStartPos, width, height};
        }
        return null;
    }


    /**
     * Reads the texel portion of the .ptm file, starting at startPos. Will read as appropriate for the file type,
     * and return an array of texel data. the array is 3D,with 1D being colour, (red, green. blue), 2D being an
     * array of length width*height, 3D being the 6 coefficients a0-a5 for the PTM polynomials.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @param startPos              position in file to start reading texel data from
     * @param width                 width of image
     * @param height                height of image
     * @return                      3D texel data array
     * @throws IOException          if there's an error trying to access the file
     * @throws PTMFileException     if there's an error parsing the .ptm file
     */
    private static int[][][] getTexelData(String fileName, String format,
                                          int startPos, int width, int height) throws IOException, PTMFileException{
        //arrays to store coefficients for each colour, all file types will eventually return these

        int[][] redVals1 = new int[width * height][3];
        int[][] redVals2 = new int[width * height][3];
        int[][] greenVals1 = new int[width * height][3];
        int[][] greenVals2 = new int[width * height][3];
        int[][] blueVals1 = new int[width * height][3];
        int[][] blueVals2 = new int[width * height][3];

        /*
        IntBuffer redVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer redVals2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer greenVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer greenVals2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer blueVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer blueVals2 = BufferUtils.createIntBuffer(width * height * 3);
        */

        //for the PTM_FORMAT_RGB file type
        if(format.equals("PTM_FORMAT_RGB")) {
            //make a scanner to scan in all the data as characters
            ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
            stream.skip(startPos);

            //for RGB files, there are 6 basis for each texel
            int basisTerm = 6;

            try {
                int offset;
                int nextCharValue;
                //loop through the colours
                for (int j = 0; j < 3; j++) {
                    //loop through y positions backwards
                    for (int y = height - 1; y >= 0; y--) {
                        //loop through x positions
                        for (int x = 0; x < width; x++) {
                            offset = (y * width) + x;
                            for (int i = 0; i < basisTerm; i++) {
                                //read the next character and convert it as per the bias
                                nextCharValue = stream.read();
                                nextCharValue = (int) ((nextCharValue - biasCoeffs[i]) * scaleCoeffs[i]);
                                //store the value in the correct array
                                if (j == 0) {
                                    if(i < 3){
                                        redVals1[offset][i] = nextCharValue;
                                        //redVals1.put(offset + i, nextCharValue);
                                    }
                                    else{
                                        redVals2[offset][i - 3] = nextCharValue;
                                        //redVals2.put(offset + i, nextCharValue);
                                    }
                                } else if (j == 1) {
                                    if(i < 3){
                                        greenVals1[offset][i] = nextCharValue;
                                        //greenVals1.put(offset + i, nextCharValue);
                                    }
                                    else{
                                        greenVals2[offset][i - 3] = nextCharValue;
                                        //greenVals2.put(offset + i, nextCharValue);
                                    }
                                } else {
                                    if(i < 3){
                                        blueVals1[offset][i] = nextCharValue;
                                        //blueVals1.put(offset + i, nextCharValue);
                                    }
                                    else{
                                        blueVals2[offset][i - 3] = nextCharValue;
                                        //blueVals2.put(offset + i, nextCharValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                throw new PTMFileException("Error reading in texel data from file");
            }
            stream.close();
            return new int[][][]{redVals1, redVals2, greenVals1, greenVals2, blueVals1, blueVals2};
        }

        return null;
    }



}
