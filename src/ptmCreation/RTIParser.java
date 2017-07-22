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
 * RTI/PTM files are plain-text files. Links to the documents for the format specification of both files are
 * given in the user guide for this app.
 *
 *
 * @author Jed Mills
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
     * Reads the .ptm/.rti file passed in the fileName argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new RTIObject and returns that.
     *
     * @param fileName                          the path to the .ptm/.rti file
     * @param mipMappingLevel                   the mip mapping level to reduce the parsed RTIObject to
     * @return                                  a new ptmCreation.RTIObject using the data in he .ptm file
     * @throws IOException                      if there's an error when trying to access the file
     * @throws RTICreator.RTIFileException      if there's an error in file type/format/parsing the file
     */
    public static RTIObject createPtmFromFile(String fileName, int mipMappingLevel) throws IOException,
            RTICreator.RTIFileException, RuntimeException {
        if(!(fileName.endsWith(".ptm") || fileName.endsWith(".rti"))){
            throw new RTICreator.RTIFileException("Only '.rti' and '.ptm' files accepted.");
        }

        //check the version and format of the file, and get the file format
        String format = getFileFormat(fileName);

        //get the PTM version, file format, width, height, and coefficients
        int[] headerData = getHeaderData(fileName, format);


        //with mip mapping, the width and height of the RTIObject are halved per mip mapping level
        int finalWidth;
        int finalHeight;

        if(format.equals("HSH")){
            //the headerDats has a different order out output numbers for HSH files, so get the actual ones here
            finalWidth = (int)  Math.floor((headerData[0]) / (Math.pow(2, mipMappingLevel)));
            finalHeight = (int) Math.floor((headerData[1]) / (Math.pow(2, mipMappingLevel)));
        }else{
            finalWidth = (int)  Math.floor((headerData[1]) / (Math.pow(2, mipMappingLevel)));
            finalHeight = (int) Math.floor((headerData[2]) / (Math.pow(2, mipMappingLevel)));
        }



        if(format.equals("PTM_FORMAT_RGB")) {
            //get the 6 coefficients per colour per pixel
            IntBuffer[] texelData = getTexelDataRGB(fileName, format, headerData[0], headerData[1],
                    headerData[2], mipMappingLevel);

            return new PTMObjectRGB(fileName, finalWidth, finalHeight, texelData);

        }else if(format.equals("PTM_FORMAT_LRGB")){
            //get the 6 lum coeffs and 3 rgb coeffs per pixel
            IntBuffer[] texelData = getTexelDataLRGB(fileName, format, headerData[0], headerData[1],
                    headerData[2], mipMappingLevel);

            return new PTMObjectLRGB(fileName, finalWidth, finalHeight, texelData);

        }else if(format.equals("PTM_FORMAT_JPEG_LRGB")){
            //decode the jpeg, then get the 6 lum coeffs and 3 rgb coeffs per pixel
            IntBuffer[] texelData = getTexelDataJPEGLRGB(fileName, headerData, mipMappingLevel);

            return new PTMObjectLRGB(fileName, finalWidth, finalHeight, texelData);

        }else if(format.equals("HSH")){
            //get the varying number (depending on basis terms) of HSH coeffs per pixel for HSH
            FloatBuffer[] texelData = getTexelDataHSH(fileName, headerData[0], headerData[1],
                    headerData[3], headerData[6], mipMappingLevel);

            return new RTIObjectHSH(fileName, finalWidth, finalHeight, headerData[2],
                                    headerData[3], headerData[4], texelData);
        }

        return null;
    }


    /**
     * Reads the .ptm/.rti file passed in the fileName argument, checks the header etc., reads the data and
     * scales/biases as appropriate to the file type, creates the new RTIObject and returns that.
     *
     * @param file                              the .ptm/.rti file
     * @param mipMappingLevel                   the mip mapping level to reduce the parsed RTIObject to
     * @return                                  a new ptmCreation.RTIObject using the data in he .ptm file
     * @throws IOException                      if there's an error when trying to access the file
     * @throws RTICreator.RTIFileException      if there's an error in file type/format/parsing the file
     */
    public static RTIObject createPtmFromFile(File file, int mipMappingLevel) throws IOException, RTICreator.RTIFileException {
        return createPtmFromFile(file.getAbsolutePath(), mipMappingLevel);
    }




    /**
     * Checks that the PTM version and format type are in the accepted lists (see attributes), and throws
     * a ptmCreation.RTICreator.RTIFileException if they aren't. Returns the file format if everything ok.
     *
     * @param fileName              path to the .ptm file
     * @return                      the file format,sound on line 2 in a ptm file
     * @throws IOException          if there's an error trying to access the file
     * @throws RTICreator.RTIFileException     if the PTM version or format are not in the accepted types
     */
    private static String getFileFormat(String fileName) throws IOException, RTICreator.RTIFileException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String version = reader.readLine();

        //there should be a PTM version declaration on line 1, check it's 1.2
        if(!Utils.checkIn(version, acceptedVersions)){
            throw new RTICreator.RTIFileException("File does not contain accepted version on line 1");
        }

        if(fileName.endsWith(".ptm")){
            if(version.equals("PTM_1.2")) {
                //the next line has the file format
                String fileFormat = reader.readLine();

                //check the format's an accepted version
                if (!Utils.checkIn(fileFormat, acceptedFormats)) {
                    throw new RTICreator.RTIFileException("File does nor contain accepted format on line 2");
                }
                reader.close();
                return fileFormat;
            }
        }else if(fileName.endsWith(".rti")){
            while(version.startsWith("#")){
                version = reader.readLine();
            }
            if(version.equals("3")){
                //great! let's get the data from the file
                reader.close();
                return "HSH";
            }else{
                //we can only accept version 3
                throw new RTICreator.RTIFileException("File contain unaccepted RTI type: " + version);
            }
        }
        reader.close();

        return null;
    }


    /**
     * Parses the header of the PTM/RTI file as appropriate to the file type. Returns an array containing, for
     * uncompressed PTM files:
     * <ol>
     *     <li>dataStartPos (start position of the texel data for reading)</li>
     *     <li>width (width of image)</li>
     *     <li>height (height of image)</li>
     * </ol>
     *
     * For compressed PTM files, the header data is:
     * <ol>
     *     <li>dataStartPos (start position of the texel data for reading)</li>
     *     <li>width (width of image)</li>
     *     <li>height (height of image)</li>
     *     <li>the next 45 compression parameters, see the PTM file specification link in the user guide</li>
     * </ol>
     *
     * For HSH files:
     * <ol>
     *     <li>width (width of image)</li>
     *     <li>height (height of image)</li>
     *     <li>colour channels (number of colour channels in the image, almost always 3)</li>
     *     <li>basis terms (number) of HSH coeffs per pixel, equal to HSH order squared</li>
     *     <li>basis type, just another HSH header data</li>
     *     <li>element size, almost always 1</li>
     *     <li>dataStartPos (start position of the texel data for reading)</li>
     * </ol>
     *
     * Throws a ptmCreation.RTICreator.RTIFileException if there's some parsing error.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @return                      an array containing {dataStartPosition, width, height}
     * @throws IOException          if there's an error trying to access the file
     * @throws RTICreator.RTIFileException     if there's an error parsing the .ptm file
     */
    private static int[] getHeaderData(String fileName, String format) throws IOException, RTICreator.RTIFileException {
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

            //read through all the comments at the start of the file starting with '#'
            String line = reader.readLine();
            int startPos = line.length() + 2;

            while(line.startsWith("#")){
                line = reader.readLine();
                startPos += line.length() + 2;
            }

            //contains the width, height, colour channel fo the HSH image
            String firstLine = reader.readLine();
            startPos += firstLine.length() + 2;

            String[] data = firstLine.split("\\s");
            int width = Integer.parseInt(data[0]);
            int height = Integer.parseInt(data[1]);
            int colourChannels = Integer.parseInt(data[2]);

            //contains the basisTerms, basisType, element size for the HSH image
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


    /**
     * Gets the header section that is at the start of PTM RGB and PTM LRGB files. This header is also at the start of
     * PTM JPEG LRGB files, but those have an extra header section after the standard header containing the compression
     * parameters. Returns the header data in the order:
     * <ol>
     *     <li>dataStartPos (start position of the texel data for reading)</li>
     *     <li>width (width of image)</li>
     *     <li>height (height of image)</li>
     * </ol>
     *
     * Stores the six bias and scale coeffs in the {@link RTIParser#biasCoeffs} and {@link RTIParser#scaleCoeffs}
     * attributes.
     *
     * @param reader                            reader containing the read file data
     * @return                                  the header data in the format specified above
     * @throws IOException                      if there's an error accessing the file
     * @throws RTICreator.RTIFileException      if there's an error parsing the file, usually from a malformed file
     */
    private static int[] getStandardPTMHeader(BufferedReader reader) throws IOException, RTICreator.RTIFileException {
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
            throw new RTICreator.RTIFileException("Error parsing the header data from file");
        }

        return new int[]{dataStartPos, width, height};
    }


    /**
     * Reads the header data about the compression factor from the BufferedReader. The reader must already be set
     * at the position for the start of the compression header, ie. it must have already read pas the standard header
     * at the start of the file.
     * <ol>
     *     <li>9 JPEG transform value</li>
     *     <li>9 JPEG order values</li>
     *     <li>9 reference plane values</li>
     *     <li>9 compressed sizes values</li>
     *     <li>9 'side data' values</li>
     * </ol>
     *
     * @param reader                        the reader to read the data from, set at the start pos of this header
     * @return                              compression data in the format specified above
     * @throws IOException                  if there's an error accessing the file
     * @throws RTICreator.RTIFileException  if there's an error parsing the file
     */
    private static int[][] getJPEGLRGBHeader(BufferedReader reader) throws IOException, RTICreator.RTIFileException {
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
            int[] transforms = Utils.intsFromStrings(items, 9);
            data[2] = transforms;

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
            throw new RTICreator.RTIFileException("Error when parsing header data from file.");
        }
    }




    /**
     * Reads the texel portion of the RGB .ptm file, starting at startPos. The returned IntBuffer array has 6
     * elements:
     * <ol>
     *     <li>IntBuffer containing the first 3 values for the red channel for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the second 3 values for the red channel for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the first 3 values for the green channel for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the second 3 values for the green channel for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the first 3 values for the blue channel for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the second 3 values for the blue channel for each pixel, 2D flattened to 1D</li>
     * </ol>
     *
     * The length of the returned IntBuffers will be (width * height * 3) / (2 ^ mipMapping), as this will also mip
     * map the data for you.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @param startPos              position in file to start reading texel data from
     * @param width                 width of image
     * @param height                height of image
     * @param mipMapping            the level of mip mapping the RTIObject data should be mipped to
     * @return                      3D texel data array
     * @throws IOException          if there's an error trying to access the file
     * @throws RTICreator.RTIFileException     if there's an error parsing the .ptm file
     */
    private static IntBuffer[] getTexelDataRGB(String fileName, String format, int startPos, int width,
                                               int height, int mipMapping) throws IOException, RTICreator.RTIFileException {
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
            throw new RTICreator.RTIFileException("Error reading in texel data from file");
        }
        stream.close();

        //if the mip mapping level is 0, there's no mip mapping, other mip that map
        if(mipMapping > 0){
            redVals1 = calcMipMapping(redVals1, width, height, mipMapping);
            redVals2 = calcMipMapping(redVals2, width, height, mipMapping);
            greenVals1 = calcMipMapping(greenVals1, width, height, mipMapping);
            greenVals2 = calcMipMapping(greenVals2, width, height, mipMapping);
            blueVals1 = calcMipMapping(blueVals1, width, height, mipMapping);
            blueVals2 = calcMipMapping(blueVals2, width, height, mipMapping);
        }

        return new IntBuffer[]{redVals1, redVals2, greenVals1, greenVals2, blueVals1, blueVals2};
    }




    /**
     * Mip maps a flattened IntBuffer of data, for a RTIObject of the given width and height. The mip mapping
     * averages blocks of 4 coefficients' values  so that the returned IntBuffer is one quarter of the size
     * of the passed IntBuffer, representing an RTIObject of width / 2, height / 2. The mipMapLevel is the desired
     * level of mapping, 1 being the first mip map, 2 being the second mip map etc..
     *
     * @param data          flattened data of RTIObject with width and height to mip
     * @param width         width of the data that is flattened in the data arg
     * @param height        height of the data that is flattened in the data arg
     * @param mapLevel      level of mip mapping desired
     * @return              the data, mipped
     */
    private static IntBuffer calcMipMapping(IntBuffer data, int width, int height, int mapLevel){
        if(mapLevel <= 0){return data;}

        //the next mip mapping level will have height and width  half of the last level
        int newWidth = (int) (Math.floor(width / 2.0f));
        int newHeight = (int) (Math.floor(height) / 2.0f);

        //the flattened data array of the next mip map
        IntBuffer mipMap = BufferUtils.createIntBuffer((int)Math.ceil(data.capacity() / 4.0f));

        //go through every other x and every other y pixel in the last map level
        int[] avgCoeffs;
        int offset;
        for(int y = 0; y < height - 1; y += 2){
            for(int x = 0; x < width - 1; x += 2){
                //get the position to set the block in the new mip level
                offset = (((y / 2) * newWidth) + (x / 2)) * 3;
                //average the block of 4 in the last mip level
                avgCoeffs = averageAroundPixel(data, width, height, x, y);
                //and store it in the single pixel of the next level
                for(int i = 0; i < 3; i ++){mipMap.put(offset + i, avgCoeffs[i]);}
            }
        }

        //if we've reached the end of the mipping of the maps return it
        if(mapLevel == 1){
            return mipMap;
        }else{
            //otherwise we need to go deeper
            return calcMipMapping(mipMap, newWidth, newHeight, mapLevel - 1);
        }
    }



    /**
     * Returns the average values of the 3 coefficients in the data buffer at position (x, y). The average is
     * found by averaging pixel coeffs in a block of 2x2 around the pixel (right and down).
     *
     * @param data      data array to average at point (x, y)
     * @param width     width of the RTIObject that the data buffer is for
     * @param height    height of the RTIObject that the data bufferis for
     * @param x         x position to average around
     * @param y         y position to average around
     * @return          the averaged 3 coefficients in the block around point (x, y)
     */
    private static int[] averageAroundPixel(IntBuffer data, int width, int height, int x, int y){
        int offset;
        int[] averages = {0, 0, 0};
        for(int dy = 0; dy < 2; dy ++){
            for(int dx = 0; dx < 2; dx++){
                //get the 1D position in the flattened array
                offset = (((y + dy) * width) + (x + dx)) * 3;
                for(int i = 0; i < 3; i++){averages[i] += data.get(offset + i);}
            }
        }
        //divide by the block of 4 we've added up to find average
        averages[0] /= 4;
        averages[1] /= 4;
        averages[2] /= 4;

        return averages;
    }


    /**
     * Reads the texel portion of the RGB .ptm file, starting at startPos. The returned IntBuffer array has 6
     * elements:
     * <ol>
     *     <li>IntBuffer containing the first 3 values for the luminance for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the second 3 values for the luminance for each pixel, 2D flattened to 1D</li>
     *     <li>IntBuffer containing the 3 rgb values for each pixel, 2D flattened to 1D</li>
     * </ol>
     *
     * The length of the returned IntBuffers will be (width * height * 3) / (2 ^ mipMapping), as this will also mip
     * map the data for you.
     *
     * @param fileName              the path to the .ptm file
     * @param format                the format of the .ptm file, see acceptedFormats
     * @param startPos              position in file to start reading texel data from
     * @param width                 width of image
     * @param height                height of image
     * @param mipMappingLevel       the level of mip mapping the RTIObject data should be mipped to
     * @return                      3D texel data array
     * @throws IOException          if there's an error trying to access the file
     * @throws RTICreator.RTIFileException     if there's an error parsing the .ptm file
     */
    private static IntBuffer[] getTexelDataLRGB(String fileName, String format, int startPos, int width,
                                                int height, int mipMappingLevel) throws IOException, RTICreator.RTIFileException {
        IntBuffer ptmCoeffs1 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer ptmCoeffs2 = BufferUtils.createIntBuffer(width * height * 3);
        IntBuffer rgbCoeffs = BufferUtils.createIntBuffer(width * height * 3);


        //make a scanner to scan in all the data as characters
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
        stream.skip(startPos);


        try{
            int offset;
            int nextCharValue;
            //loop through y positions backwards
            for(int y = height - 1; y >= 0; y--){
                //loop through x positions
                for(int x = 0; x < width; x++){
                    offset = ((y * width) + x) * 3;

                    for(int i = 0; i < 6; i++){
                        //read the next character and convert it as per the bias
                        nextCharValue = stream.read();
                        nextCharValue = (int) ((nextCharValue - biasCoeffs[i]) * scaleCoeffs[i]);

                        //all the luminance coefficients come in a block before the rgb coeffs
                        if(i < 3){ptmCoeffs1.put(offset + i, nextCharValue);}
                        else{ptmCoeffs2.put(offset + i - 3, nextCharValue);}
                    }
                }
            }
            //now for the rgb coeffs
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
            throw new RTICreator.RTIFileException("Error reading in texel data from file");
        }

        stream.close();


        //if the mip mapping level is 0, there's no mip mapping, other mip that map
        if(mipMappingLevel > 0){
            ptmCoeffs1 = calcMipMapping(ptmCoeffs1, width, height, mipMappingLevel);
            ptmCoeffs2 = calcMipMapping(ptmCoeffs2, width, height, mipMappingLevel);
            rgbCoeffs = calcMipMapping(rgbCoeffs, width, height, mipMappingLevel);
        }

        return new IntBuffer[]{ptmCoeffs1, ptmCoeffs2, rgbCoeffs};
    }



    /**
     * Mip maps a flattened FloatBuffer of data, for a RTIObject of the given width and height. The mip mapping
     * averages blocks of 4 coefficients' values  so that the returned IntBuffer is one quarter of the size
     * of the passed IntBuffer, representing an RTIObject of width / 2, height / 2. The mipMapLevel is the desired
     * level of mapping, 1 being the first mip map, 2 being the second mip map etc..
     *
     * @param data          flattened data of RTIObject with width and height to mip
     * @param width         width of the data that is flattened in the data arg
     * @param height        height of the data that is flattened in the data arg
     * @param mapLevel      level of mip mapping desired
     * @return              the data, mipped
     */
    private static FloatBuffer calcMipMapping(FloatBuffer data, int width, int height, int mapLevel){
        //the next mip mapping level will have height and width  half of the last level
        int newWidth = (int) (Math.floor(width / 2.0f));
        int newHeight = (int) (Math.floor(height) / 2.0f);

        //the flattened data array of the next mip map
        FloatBuffer mipMap = BufferUtils.createFloatBuffer((int)Math.ceil(data.capacity() / 4.0f));

        //go through every other x and every other y pixel in the last map level
        float[] avgCoeffs;
        int offset;
        for(int y = 0; y < height - 1; y += 2){
            for(int x = 0; x < width - 1; x += 2){
                //get the position to set the block in the new mip level
                offset = (((y / 2) * newWidth) + (x / 2)) * 3;
                //average the block of 4 in the last mip level
                avgCoeffs = averageAroundPixel(data, width, height, x, y);
                //and store it in the single pixel of the next level
                for(int i = 0; i < 3; i ++){mipMap.put(offset + i, avgCoeffs[i]);}
            }
        }

        //if we've reached the end of the mipping of the maps return it
        if(mapLevel == 1){
            return mipMap;
        }else{
            //otherwise we need to go deeper
            return calcMipMapping(mipMap, newWidth, newHeight, mapLevel - 1);
        }
    }


    /**
     * Returns the average values of the 3 coefficients in the data buffer at position (x, y). The average is
     * found by averaging pixel coeffs in a block of 2x2 around the pixel (right and down).
     *
     * @param data      data array to average at point (x, y)
     * @param width     width of the RTIObject that the data buffer is for
     * @param height    height of the RTIObject that the data bufferis for
     * @param x         x position to average around
     * @param y         y position to average around
     * @return          the averaged 3 coefficients in the block around point (x, y)
     */
    private static float[] averageAroundPixel(FloatBuffer data, int width, int height, int x, int y){
        int offset;
        float[] averages = {0, 0, 0};
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                //get the 1D position in the flattened array
                offset = (((y + dy) * width) + (x + dx)) * 3;
                for (int i = 0; i < 3; i++) {
                    averages[i] += data.get(offset + i);
                }
            }
        }
        //divide by the block of 4 we've added up to find average
        averages[0] /= 4.0f;
        averages[1] /= 4.0f;
        averages[2] /= 4.0f;

        return averages;
    }


    /**
     * Reads the texel portion of the HSH .rti file, starting at startPos. The returned FloatBuffer array has 9
     * elements. If the basisTerms arg is <= 3 , the FloatBuffers in the textData attribute marked with a * or ** below
     * will be of length 3, with 3 zeros in, as these textures won't be used by the OpenGl shaders to render, but still
     * need to be bound, even is they have one element in. If basisTerms is <= 6 terms, FloatBuffers marked with
     * ** will be of length 3 with 3 zeros.
     *
     * <ol>
     *     <li>an IntBuffer containing the first 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     *
     *     <li>an IntBuffer containing the first 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     *
     *     <li>an IntBuffer containing the first 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     * </ol>
     *
     * The length of the returned IntBuffers will be (width * height * 3) / (2 ^ mipMapping), as this will also mip
     * map the data for you.
     *
     * @param fileName                          the path to the .ptm file
     * @param startPos                          position in file to start reading texel data from
     * @param width                             width of image
     * @param height                            height of image
     * @param basisTerms                        number of HSH basis terms per pixel
     * @param mipMappingLevel                   the level of mip mapping the RTIObject data should be mipped to
     * @return                                  3D texel data array
     * @throws IOException                      if there's an error trying to access the file
     * @throws RTICreator.RTIFileException      if there's an error parsing the .ptm file
     */
    private static FloatBuffer[] getTexelDataHSH(String fileName, int width, int height,
                     int basisTerms, int startPos, int mipMappingLevel) throws IOException{

        //need a little endian stream to read the floats for the scale and bias values
        LittleEndianDataInputStream leStream = new LittleEndianDataInputStream(new FileInputStream(fileName));

        //skip the header to the start of the data
        leStream.skip(startPos);
        float[] scale = new float[basisTerms];
        float[] bias = new float[basisTerms];

        //read the scale coeffs
        for(int i = 0; i < basisTerms; i++){scale[i] = leStream.readFloat();}

        //read the bias coeffs
        for(int i = 0; i < basisTerms; i++){bias[i] = leStream.readFloat();}

        //the rest of the data has no endianness and the little endian reader is really slow so we'll
        //use a standard reader for the rest of the data
        leStream.close();

        //ready to read the bulk of data
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)));
        stream.skip(startPos + (2 * 4 * basisTerms));

        int capacity = width * height * 3;

        FloatBuffer redCoeffs1 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs1 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs1 = BufferUtils.createFloatBuffer(capacity);

        //if there are 3 or less coeffs per pixel, we don't need the next buffers for data, but we'll make them
        //of size 3 because OpenGL has to have a texture with at least one 3D element in it
        if(basisTerms < 4){capacity = 3;}

        FloatBuffer redCoeffs2 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs2 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs2 = BufferUtils.createFloatBuffer(capacity);

        //same thing for 6 or less coeffs
        if(basisTerms < 7){capacity = 3;}

        FloatBuffer redCoeffs3 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer greenCoeffs3 = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer blueCoeffs3 = BufferUtils.createFloatBuffer(capacity);


        //loopthrough all the stuff an read it into the relevant buffer
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


        //only mip map the buffers we've actually put stuff in
        if(mipMappingLevel > 0){
            redCoeffs1 = calcMipMapping(redCoeffs1, width, height, mipMappingLevel);
            greenCoeffs1 = calcMipMapping(greenCoeffs1, width, height, mipMappingLevel);
            blueCoeffs1 = calcMipMapping(blueCoeffs1, width, height, mipMappingLevel);

            if(basisTerms > 3){
                redCoeffs2 = calcMipMapping(redCoeffs2, width, height, mipMappingLevel);
                greenCoeffs2 = calcMipMapping(greenCoeffs2, width, height, mipMappingLevel);
                blueCoeffs2 = calcMipMapping(blueCoeffs2, width, height, mipMappingLevel);
            }

            if(basisTerms > 6){
                redCoeffs3 = calcMipMapping(redCoeffs3, width, height, mipMappingLevel);
                greenCoeffs3 = calcMipMapping(greenCoeffs3, width, height, mipMappingLevel);
                blueCoeffs3 = calcMipMapping(blueCoeffs3, width, height, mipMappingLevel);
            }
        }


        //finally done!
        return new FloatBuffer[]{redCoeffs1,    redCoeffs2,     redCoeffs3,
                                 greenCoeffs1,  greenCoeffs2,   greenCoeffs3,
                                 blueCoeffs1,   blueCoeffs2,    blueCoeffs3};

    }


    /**
     * Reads the texel portion of the compressed LRGB .ptm file, starting at startPos, decompresses it, and
     * returns the standard, uncompressed LRGB coeff arrays. The length of the returned IntBuffers will be
     * (width * height * 3) / (2 ^ mipMapping), as this will also mip map the data for you.
     *
     * @param fileName              the path to the .ptm file
     * @param headerData            header data in the specified order given in {@link RTIParser#getHeaderData(String, String)}
     * @param mipMappingLevel       the level of mip mapping the RTIObject data should be mipped to
     * @return                      3D texel data array
     * @throws IOException          if there's an error trying to access the file
     * @throws RTICreator.RTIFileException     if there's an error parsing the .ptm file
     */
    private static IntBuffer[] getTexelDataJPEGLRGB(String fileName, int[] headerData, int mipMappingLevel)
                                                        throws IOException, RTICreator.RTIFileException, RuntimeException{
        //all this is important jpeg stuff I think
        int dataStartPos = headerData[0];
        int width = headerData[1];
        int height = headerData[2];
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

            //here's where the actual decoding happens of the 9 planes
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(compressedPlane));
            BufferedImage image = decoder.decodeAsBufferedImage();
            Utils.flip(image);
            planeLength[i] = image.getHeight() * image.getWidth();
            plane[i] = new int[planeLength[i]];
            for(int j = 0; j < planeLength[i]; j++){
                plane[i][j] = image.getRaster().getDataBuffer().getElem(j);
            }
        }

        //now go through the 9 decompressed planes and do some more jpeg stuff to turn them into the
        //actual coefficients
        int[][] coeffs = new int[9][];
        int index;
        for(int i = 0; i < 9; i++){
            index = Utils.indexOf(orderParams, i, 9);
            if(index == -1){
                throw new RTICreator.RTIFileException("Error parsing compressed texel data from file.");
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

        //now we have the actual coefficients from the file, we canput them into the standard LRGB arrays
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

        //dothe usual mip mapping if required
        if(mipMappingLevel > 0){
            ptmCoeffs1 = calcMipMapping(ptmCoeffs1, width, height, mipMappingLevel);
            ptmCoeffs2 = calcMipMapping(ptmCoeffs2, width, height, mipMappingLevel);
            rgbCoeffs = calcMipMapping(rgbCoeffs, width, height, mipMappingLevel);
        }

        return new IntBuffer[]{ptmCoeffs1, ptmCoeffs2, rgbCoeffs};
    }

}
