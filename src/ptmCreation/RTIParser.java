package ptmCreation;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import utils.Utils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * This class is responsible for parsing pm files and creating PTMObjects from the data. The parser accepts
 * PTM version 1.2, and the following file formats:
 *      PTM_FORMAT_RGB
 *      PTM_FORMAT_LRGB
 *      #HSH1.2
 *
 * Created by jed on 14/05/17.
 */
public class RTIParser {

    /**Used for checking the PTM version fo the file used*/
    private static final String[] acceptedVersions = new String[]{"PTM_1.2", "#HSH1.2"};

    /**Used for checking the PTM format of the passed file*/
    private static final String[] acceptedFormats = new String[]{"PTM_FORMAT_RGB", "PTM_FORMAT_LRGB",
                                                                "PTM_FORMAT_JPEG_LRGB", "#HSH1.2"};

    /**Scaling coefficients used for RGB format found in file header*/
    private static float[] scaleCoeffs;

    /**Bias coefficients for RGB format found in file header*/
    private static int[] biasCoeffs;


    /**
     * Reads the .ptm file passed in the fileName argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new ptmCreation.RTIObject and returns that.
     *
     * @param fileName              the path to the .ptm file
     * @return                      a new ptmCreation.RTIObject using the data in he .ptm file
     * @throws IOException          if there's an error when trying to access the file
     * @throws RTIFileException     if there's an error in file type/format/parsing the file
     */
    public static RTIObject createPtmFromFile(String fileName) throws IOException, RTIFileException, RuntimeException {
        if(!(fileName.endsWith(".ptm") || fileName.endsWith(".rti"))){
            throw new RTIFileException("Only '.rti' and '.ptm' files accepted.");
        }

        //check the version and format of the file, and get the file format
        String format = getFileFormat(fileName);

        //get the PTM version, file format, width, height, and coefficients
        int[] headerData = getHeaderData(fileName, format);

        if(format.equals("PTM_FORMAT_RGB")) {
            //get the coefficients for the individual texels
            IntBuffer[] texelData = getTexelDataRGB(fileName, format, headerData[0], headerData[1], headerData[2]);

            //create the ptmCreation.RTIObject from the data
            return new PTMObjectRGB(fileName, headerData[1], headerData[2], texelData);

        }else if(format.equals("PTM_FORMAT_LRGB")){
            IntBuffer[] texelData = getTexelDataLRGB(fileName, format, headerData[0], headerData[1], headerData[2]);

            return new PTMObjectLRGB(fileName, headerData[1], headerData[2], texelData);

        }else if(format.equals("PTM_FORMAT_JPEG_LRGB")){
            IntBuffer[] texelData = getTexelDataJPEGLRGB(fileName, headerData);

            return new PTMObjectLRGB(fileName, headerData[1], headerData[2], texelData);
        }else if(format.equals("HSH")){
            FloatBuffer[] texelData = getTexelDataHSH(fileName, headerData[0], headerData[1], headerData[2],
                                                    headerData[3], headerData[4], headerData[5], headerData[6]);

            return new RTIObjectHSH(fileName, headerData[0], headerData[1], headerData[2],
                                    headerData[3], headerData[4], texelData);
        }

        return null;
    }


    /**
     * Reads the .ptm file passed in the file argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new ptmCreation.RTIObject and returns that.
     *
     * @param file                  the file to create the PTM object from
     * @return                      a new ptmCreation.RTIObject using the data in he .ptmfile
     * @throws IOException          if there's an error when trying to access the file
     * @throws RTIFileException     if there's an error in file type/format/parsing the file
     */
    public static RTIObject createPtmFromFile(File file) throws IOException, RTIFileException {
        return createPtmFromFile(file.getAbsolutePath());
    }


    /**
     * Checks that the PTM version and format type are in the accepted lists (see attributes), and throws
     * a ptmCreation.RTIFileException if they aren't. Returns the file format if everything ok.
     *
     * @param fileName              path to the .ptm file
     * @return                      the file format,sound on line 2 in a ptm file
     * @throws IOException          if there's an error trying to access the file
     * @throws RTIFileException     if the PTM version or format are not in the accepted types
     */
    private static String getFileFormat(String fileName) throws IOException, RTIFileException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String version = reader.readLine();

        //there should be a PTM version declaration on line 1, check it's 1.2
        if(!Utils.checkIn(version, acceptedVersions)){
            throw new RTIFileException("File does not contain accepted version on line 1");
        }

        if(fileName.endsWith(".ptm")){
            if(version.equals("PTM_1.2")) {
                //the next line has the file format
                String fileFormat = reader.readLine();

                //check the format's an accepted version
                if (!Utils.checkIn(fileFormat, acceptedFormats)) {
                    throw new RTIFileException("File does nor contain accepted format on line 2");
                }
                reader.close();
                return fileFormat;
            }
        }else if(fileName.endsWith(".rti")){
            while(version.startsWith("#")){
                version = reader.readLine();
            }
            if(version.equals("3")){
                reader.close();
                return "HSH";
            }else{
                throw new RTIFileException("File contain unaccepted RTI type: " + version);
            }
        }
        reader.close();

        return null;
    }


    /**
     * Parses the header of the PTM file as appropriate to the file type. Returns an array containing
     * dataStartPos (start position of the texel data for reading), width (width of image), height
     * (height of image). Throws a ptmCreation.RTIFileException if there's some parsing error.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @return                      an array containing {dataStartPosition, width, height}
     * @throws IOException          if there's an error trying to access the file
     * @throws RTIFileException     if there's an error parsing the .ptm file
     */
    private static int[] getHeaderData(String fileName, String format) throws IOException, RTIFileException {
        //make a read to read in the header section of the .ptm file
        BufferedReader reader = new BufferedReader(new FileReader(fileName));


        if(format.equals("PTM_FORMAT_RGB") || format.equals("PTM_FORMAT_LRGB")){
            int[] data = getStandardPTMHeader(reader);
            reader.close();
            return data;
        }else if(format.equals("PTM_FORMAT_JPEG_LRGB")){
            int[] standardHeaderData = getStandardPTMHeader(reader);
            int[][] jpegHeaderData = getJPEGLRGBHeader(reader);

            int dataStartPos = standardHeaderData[0] + jpegHeaderData[0][0];

            reader.close();
            //all the header data in total, flattened
            int[] totalHeaderData = new int[49];
            //the start position of the compressed texel data
            totalHeaderData[0] = dataStartPos;
            //the width of the image
            totalHeaderData[1] = standardHeaderData[1];
            //height of the image
            totalHeaderData[2] = standardHeaderData[2];
            //compression factor
            totalHeaderData[3] = jpegHeaderData[1][0];

            //flatten the compression parameters
            int[] flattenedParams = Utils.flatten(new int[][]{jpegHeaderData[2], jpegHeaderData[3], jpegHeaderData[4],
                                                                jpegHeaderData[5], jpegHeaderData[6]});

            for(int i = 0; i < 45; i++){
                totalHeaderData[i + 4] = flattenedParams[i];
            }
            return totalHeaderData;
        }else if(format.equals("HSH")){

            String line = reader.readLine();
            int startPos = line.length() + 2;


            while(line.startsWith("#")){
                line = reader.readLine();
                startPos += line.length() + 2;
            }

            String firstLine = reader.readLine();

            startPos += firstLine.length() + 2;
            String[] data = firstLine.split("\\s");
            int width = Integer.parseInt(data[0]);
            int height = Integer.parseInt(data[1]);
            int colourChannels = Integer.parseInt(data[2]);

            String secondLine = reader.readLine();

            startPos += secondLine.length() + 2;
            data = secondLine.split("\\s");
            int basisTerms = Integer.parseInt(data[0]);
            int basisType = Integer.parseInt(data[1]);
            int elementSize = Integer.parseInt(data[2]);


            return new int[]{width, height, colourChannels,  basisTerms, basisType, elementSize, startPos};


        }

        return null;
    }


    private static int[] getStandardPTMHeader(BufferedReader reader) throws IOException, RTIFileException {
        String header = "";
        for (int i = 0; i < 6; i++) {
            header += reader.readLine() + " ";
        }
        int dataStartPos = header.length();

        //split the header into each item in it
        String[] items = header.split("(\\s+)|(\\n+)");

        //get RTI type, file type, image width, image height
        int width, height;
        scaleCoeffs = new float[6];
        biasCoeffs = new int[6];
        try {
            width = Integer.parseInt(items[2]);
            height = Integer.parseInt(items[3]);

            //get the 6 scaling coefficients
            for (int i = 0; i < 6; i++) {
                scaleCoeffs[i] = Float.parseFloat(items[i + 4]);
            }
            //get the six bias coefficients
            for (int i = 0; i < 6; i++) {
                biasCoeffs[i] = Integer.parseInt(items[i + 10]);
            }
        } catch (NumberFormatException e) {
            throw new RTIFileException("Error parsing the header data from file");
        }

        return new int[]{dataStartPos, width, height};
    }


    private static int[][] getJPEGLRGBHeader(BufferedReader reader) throws IOException, RTIFileException {
        String header = "";
        int[][] data = new int[7][];

        String line;
        try {
            //get the compression parameter
            line = reader.readLine();
            header += line + " ";
            int compressParam = Integer.parseInt(line);
            data[1] = new int[]{compressParam};

            //read and parse the 9 JPEG transforms on next line
            line = reader.readLine();
            header += line + " ";
            String[] items = line.split("(\\s+)");
            int[] transforsms = Utils.intsFromStrings(items, 9);
            data[2] = transforsms;

            //read the motion vector lines 1 and 2, not needed here
            line = reader.readLine();
            header += line + " ";
            line = reader.readLine();
            header += line + " ";

            //read and parse the 9 order nums
            line = reader.readLine();
            header += line + " ";
            items = line.split("(\\s+)");
            int[] orders = Utils.intsFromStrings(items, 9);
            data[3] = orders;

            //read and parse the 9 reference plane nums
            line = reader.readLine();
            header += line + " ";
            items = line.split("(\\s+)");
            int[] refPlanes = Utils.intsFromStrings(items, 9);
            data[4] = refPlanes;

            //read and parse the 9 compressed sizes
            line = reader.readLine();
            header += line + " ";
            items = line.split("(\\s+)");
            int[] compressedSizes = Utils.intsFromStrings(items, 9);
            data[5] = compressedSizes;

            //read and parse the 9 side data nums
            line = reader.readLine();
            header += line + " ";
            items = line.split("(\\s+)");
            int[] sideData = Utils.intsFromStrings(items, 9);
            data[6] = sideData;

            int headerLength = header.length();
            data[0] = new int[]{headerLength};

            return data;
        }catch (Exception e){
            throw new RTIFileException("Error when parsing header data from file.");
        }
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
     * @throws RTIFileException     if there's an error parsing the .ptm file
     */
    private static IntBuffer[] getTexelDataRGB(String fileName, String format,
                                          int startPos, int width, int height) throws IOException, RTIFileException {
        //arrays to store coefficients for each colour, all file types will eventually return these
        IntBuffer redVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer redVals2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer greenVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer greenVals2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer blueVals1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer blueVals2 = BufferUtils.createIntBuffer(width * height * 3);

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
                        offset = ((y * width) + x) * 3;
                        for (int i = 0; i < basisTerm; i++) {
                            //read the next character and convert it as per the bias
                            nextCharValue = stream.read();
                            nextCharValue = (int) ((nextCharValue - biasCoeffs[i]) * scaleCoeffs[i]);
                            //store the value in the correct array
                            if (j == 0) {
                                if(i < 3){redVals1.put(offset + i, nextCharValue);}
                                else{redVals2.put(offset + i - 3, nextCharValue);}
                            } else if (j == 1) {
                                if(i < 3){greenVals1.put(offset + i, nextCharValue);}
                                else{greenVals2.put(offset + i - 3, nextCharValue);}
                            } else {
                                if(i < 3){blueVals1.put(offset + i, nextCharValue);}
                                else{blueVals2.put(offset + i - 3, nextCharValue);}
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            throw new RTIFileException("Error reading in texel data from file");
        }
        stream.close();

        return new IntBuffer[]{redVals1, redVals2, greenVals1, greenVals2, blueVals1, blueVals2};
    }



    private static IntBuffer[] getTexelDataLRGB(String fileName, String format,
                                         int startPos, int width, int height) throws IOException, RTIFileException {
        IntBuffer ptmCoeffs1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer ptmCoeffs2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer rgbCoeffs = BufferUtils.createIntBuffer(width * height * 3);


        //make a scanner to scan in all the data as characters
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
        stream.skip(startPos);


        try{
            int offset;
            int nextCharValue;

            for(int y = height - 1; y >= 0; y--){
                for(int x = 0; x < width; x++){
                    offset = ((y * width) + x) * 3;

                    for(int i = 0; i < 6; i++){
                        //read the next character and convert it as per the bias
                        nextCharValue = stream.read();
                        nextCharValue = (int) ((nextCharValue - biasCoeffs[i]) * scaleCoeffs[i]);

                        if(i < 3){ptmCoeffs1.put(offset + i, nextCharValue);}
                        else{ptmCoeffs2.put(offset + i - 3, nextCharValue);}
                    }
                }
            }

            for(int y = height - 1; y >= 0; y--){
                for(int x = 0; x < width; x++){
                    offset = ((y * width) + x) * 3;

                    for(int i = 0; i < 3; i++){
                        nextCharValue = stream.read();

                        rgbCoeffs.put(offset + i, nextCharValue);
                    }
                }
            }
        }catch(Exception e){
            throw new RTIFileException("Error reading in texel data from file");
        }

        stream.close();
        return new IntBuffer[]{ptmCoeffs1, ptmCoeffs2, rgbCoeffs};
    }



    private static FloatBuffer[] getTexelDataHSH(String fileName, int width, int height, int colsPerPixel,
                                     int basisTerms, int basisType, int elemSize, int startPos) throws IOException{


        LittleEndianDataInputStream leStream = new LittleEndianDataInputStream(new FileInputStream(fileName));

        leStream.skip(startPos);
        float[] scale = new float[basisTerms];
        float[] bias = new float[basisTerms];

        for(int i = 0; i < basisTerms; i++){scale[i] = leStream.readFloat();}
        for(int i = 0; i < basisTerms; i++){bias[i] = leStream.readFloat();}

        leStream.close();

        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
        stream.skip(startPos + (2 * 4 * basisTerms));

        int capacity = width * height * 3;

        FloatBuffer redCoeffs1 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs1 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs1 = BufferUtils.createFloatBuffer(capacity);

        if(basisTerms < 4){capacity = 3;}

        FloatBuffer redCoeffs2 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs2 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs2 = BufferUtils.createFloatBuffer(capacity);

        if(basisTerms < 7){capacity = 3;}

        FloatBuffer redCoeffs3 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs3 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs3 = BufferUtils.createFloatBuffer(capacity);

        int offset;
        float nextCharValue;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                offset = (y * width + x) * 3;

                for(int k = 0; k < basisTerms; k++){
                    nextCharValue = (stream.read() / 255.0f) * scale[k] + bias[k];
                    if(k < 3){redCoeffs1.put(offset + k, nextCharValue);}
                    else if(k < 6){redCoeffs2.put(offset + k - 3, nextCharValue);}
                    else if(k < 9){redCoeffs3.put(offset + k - 6, nextCharValue);}
                }

                for(int k = 0; k < basisTerms; k++){
                    nextCharValue = (stream.read() / 255.0f) * scale[k] + bias[k];
                    if(k < 3){greenCoeffs1.put(offset + k, nextCharValue);}
                    else if(k < 6){greenCoeffs2.put(offset + k - 3, nextCharValue);}
                    else if(k < 9){greenCoeffs3.put(offset + k - 6, nextCharValue);}
                }

                for(int k = 0; k < basisTerms; k++){
                    nextCharValue = (stream.read() / 255.0f) * scale[k] + bias[k];
                    if(k < 3){blueCoeffs1.put(offset + k, nextCharValue);}
                    else if(k < 6){blueCoeffs2.put(offset + k - 3, nextCharValue);}
                    else if(k < 9){blueCoeffs3.put(offset + k - 6, nextCharValue);}
                }
            }
        }
        stream.close();

        return new FloatBuffer[]{redCoeffs1,    redCoeffs2,     redCoeffs3,
                                 greenCoeffs1,  greenCoeffs2,   greenCoeffs3,
                                 blueCoeffs1,   blueCoeffs2,    blueCoeffs3};

    }



    private static IntBuffer[] getTexelDataJPEGLRGB(String fileName, int[] headerData)
                                                                        throws IOException, RTIFileException,
                                                                            RuntimeException{
        int dataStartPos = headerData[0];
        int width = headerData[1];
        int height = headerData[2];
        int compressionFactor = headerData[3];
        int[] compressionTransforms = Utils.sliceArray(headerData, 4, 13);
        int[] orderParams = Utils.sliceArray(headerData, 13, 22);
        int[] referencePlane = Utils.sliceArray(headerData, 22, 31);
        int[] compressedSizes = Utils.sliceArray(headerData, 31, 40);
        int[] sideData = Utils.sliceArray(headerData, 40, 49);


        //make a scanner to scan in all the data as characters
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
        stream.skip(dataStartPos);

        int[][] plane = new int[9][];
        int[] planeLength = new int[9];
        byte[][] info = new byte[9][];
        byte[] compressedPlane;

        for(int i = 0; i < 9; i++){
            //read the compressed plane
            compressedPlane = new byte[compressedSizes[i]];
            for(int j = 0; j < compressedSizes[i]; j ++){
                int nextVal = stream.read();
                compressedPlane[j] = (byte)nextVal;
            }

            //read the side info
            info[i] = new byte[sideData[i]];
            for(int j = 0; j < sideData[i]; j++){
                info[i][j] = (byte)stream.read();
            }

            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(compressedPlane));
            BufferedImage image = decoder.decodeAsBufferedImage();
            Utils.flip(image);
            planeLength[i] = image.getHeight() * image.getWidth();
            plane[i] = new int[planeLength[i]];
            for(int j = 0; j < planeLength[i]; j++){
                plane[i][j] = image.getRaster().getDataBuffer().getElem(j);
            }
        }

        int[][] coeffs = new int[9][];
        int index;
        for(int i = 0; i < 9; i++){
            index = Utils.indexOf(orderParams, i, 9);
            if(index == -1){
                throw new RTIFileException("Error parsing compressed texel data from file.");
            }
            if(referencePlane[index] < 0){
                coeffs[index] = new int[planeLength[index]];
                for(int j = 0; j < planeLength[index]; j++){
                    coeffs[index][j] = plane[index][j];
                }
            }else if(compressionTransforms[index] == 0){
                coeffs[index] = Utils.combine(coeffs[referencePlane[index]], plane[index], planeLength[index]);
            }else if(compressionTransforms[index] == 1){
                int[] inverse = Utils.invert(coeffs[referencePlane[index]], planeLength[referencePlane[index]]);
                coeffs[index] = Utils.combine(inverse, plane[index], planeLength[index]);
            }
            if(sideData[index] > 0){
                Utils.correctCoeff(coeffs[index], info[index], sideData[index], width, height);
            }
        }

        IntBuffer ptmCoeffs1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer ptmCoeffs2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer rgbCoeffs = BufferUtils.createIntBuffer(width * height * 3);

        int offset;
        int offset3;
        int nextVal;

        for(int y = height - 1; y >= 0; y--){
            for(int x = 0; x < width; x++){
                offset = (y * width) + x;
                offset3 = offset * 3;

                for(int i = 0; i < 6; i++){
                    nextVal = (int)((coeffs[i][offset] - biasCoeffs[i]) * scaleCoeffs[i]);
                    if(i < 3){ptmCoeffs1.put(offset3 + i, nextVal);}
                    else{ptmCoeffs2.put(offset3 + i - 3, nextVal);}
                }
                rgbCoeffs.put(offset3, coeffs[6][offset]);
                rgbCoeffs.put(offset3 + 1, coeffs[7][offset]);
                rgbCoeffs.put(offset3 + 2, coeffs[8][offset]);
            }
        }

        stream.close();
        return new IntBuffer[]{ptmCoeffs1, ptmCoeffs2, rgbCoeffs};
    }

}
