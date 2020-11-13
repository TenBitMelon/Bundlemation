import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Main {

    static int size = 50;
    static ArrayList<Color> glassColors = new ArrayList<Color>(){{
        add(new Color(103, 113, 154));
        add(new Color(154, 113, 169));
        add(new Color(124, 144, 169));
        add(new Color(93, 93, 93));
        add(new Color(124, 133, 103));
        add(new Color(184, 184, 184));
        add(new Color(113, 113, 113));
        add(new Color(145, 94, 94));
        add(new Color(169, 133, 103));
        add(new Color(113, 133, 144));
        add(new Color(133, 108, 154));
        add(new Color(133, 164, 93));
        add(new Color(174, 174, 103));
        add(new Color(179, 133, 149));
        add(new Color(124, 113, 103));
        add(new Color(144, 144, 144));
    }};
    static HashMap<Color, String> colorToGlass = new HashMap<Color, String>(){{
        put(new Color(103, 113, 154), "{id:blue_stained_glass_pane,Count:1b}");
        put(new Color(154, 113, 169), "{id:magenta_stained_glass_pane,Count:1b}");
        put(new Color(124, 144, 169), "{id:light_blue_stained_glass_pane,Count:1b}");
        put(new Color(93, 93, 93), "{id:black_stained_glass_pane,Count:1b}");
        put(new Color(124, 133, 103), "{id:green_stained_glass_pane,Count:1b}");
        put(new Color(184, 184, 184), "{id:white_stained_glass_pane,Count:1b}");
        put(new Color(113, 113, 113), "{id:gray_stained_glass_pane,Count:1b}");
        put(new Color(145, 94, 94), "{id:red_stained_glass_pane,Count:1b}");
        put(new Color(169, 133, 103), "{id:orange_stained_glass_pane,Count:1b}");
        put(new Color(113, 133, 144), "{id:cyan_stained_glass_pane,Count:1b}");
        put(new Color(133, 108, 154), "{id:purple_stained_glass_pane,Count:1b}");
        put(new Color(133, 164, 93), "{id:lime_stained_glass_pane,Count:1b}");
        put(new Color(174, 174, 103), "{id:yellow_stained_glass_pane,Count:1b}");
        put(new Color(179, 133, 149), "{id:pink_stained_glass_pane,Count:1b}");
        put(new Color(124, 113, 103), "{id:brown_stained_glass_pane,Count:1b}");
        put(new Color(144, 144, 144), "{id:light_gray_stained_glass_pane,Count:1b}");
    }};

    public static void main(String[] args) throws IOException {

        ArrayList<ImageFrame> imageFrames = new ArrayList<ImageFrame>();
        ArrayList<String> functionFileNames = new ArrayList<String>();

        InputStream inputStream;
        try {
            inputStream = new FileInputStream("src/main/java/boom.gif");
            Collections.addAll(imageFrames, readGif(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < imageFrames.size(); i++) {
            ImageFrame frame = imageFrames.get(i);
            functionFileNames.add(createFrameFile(frame, i, imageFrames.get(0).getHeight()));
        }

        FileWriter myFile = new FileWriter("init.mcfunction");
        for (int i = 0; i < functionFileNames.size(); i++) {
            myFile.write("schedule function starter:" + functionFileNames.get(i) + " " + (i + 1) + "t\n");
            if(i == functionFileNames.size() - 1) {
                myFile.write("schedule function starter:init " + (i + 1) + "t");
            }
        }
        myFile.close();

    }

    private static String createFrameFile(ImageFrame frame, int index, int sizeSize) throws IOException {
        BufferedImage frameImage = frame.getImage();
        ArrayList<Color> imageColors = new ArrayList<Color>();

        BufferedImage dbi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dbi.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance((double) size / (double) sizeSize, (double) size / (double) sizeSize);
        g.drawRenderedImage(frameImage, at);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Color color = new Color(dbi.getRGB(i, j));
                Color bestColor = null;
                Double bestColorNumber = 1000000.0;
                for (Color glassColor : glassColors) {
                    double colorDistance = colorDistance(color, glassColor);
                    if (colorDistance < bestColorNumber) {
                        bestColor = glassColor;
                        bestColorNumber = colorDistance;
                    }
                }
                imageColors.add(bestColor);
            }
        }

        String bundleItemNBT = "";

        for (Color color : imageColors) {
            bundleItemNBT = bundleItemNBT + colorToGlass.get(color) + ",";
        }
        bundleItemNBT = bundleItemNBT.substring(0, bundleItemNBT.length() - 1);

        FileWriter myFile = new FileWriter("frame_files/frame" + index + ".mcfunction");
        myFile.write("item entity @a hotbar.0 replace minecraft:bundle{Items:[" + bundleItemNBT + "]}");
        myFile.close();
        return "frame_files/frame" + index;
    }

    private static double colorDistance(Color c1, Color c2)
    {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return (((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8);
    }

    private static ImageFrame[] readGif(InputStream stream) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<ImageFrame>(2);

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(stream));

        int lastx = 0;
        int lasty = 0;

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();

        Color backgroundColor = null;

        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
            NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }

            if (globalColorTable != null && globalColorTable.getLength() > 0) {
                IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

                if (colorTable != null) {
                    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                    IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
                    while (colorEntry != null) {
                        if (colorEntry.getAttribute("index").equals(bgIndex)) {
                            int red = Integer.parseInt(colorEntry.getAttribute("red"));
                            int green = Integer.parseInt(colorEntry.getAttribute("green"));
                            int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

                            backgroundColor = new Color(red, green, blue);
                            break;
                        }

                        colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
                    }
                }
            }
        }

        BufferedImage master = null;
        boolean hasBackround = false;

        for (int frameIndex = 0; ; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            NodeList children = root.getChildNodes();

            int delay = Integer.parseInt(gce.getAttribute("delayTime"));

            String disposal = gce.getAttribute("disposalMethod");

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                master.createGraphics().setColor(backgroundColor);
                master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

                hasBackround = image.getWidth() == width && image.getHeight() == height;

                master.createGraphics().drawImage(image, 0, 0, null);
            } else {
                int x = 0;
                int y = 0;

                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);

                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();

                        x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                if (disposal.equals("restoreToPrevious")) {
                    BufferedImage from = null;
                    for (int i = frameIndex - 1; i >= 0; i--) {
                        if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                            from = frames.get(i).getImage();
                            break;
                        }
                    }

                    {
                        ColorModel model = from.getColorModel();
                        boolean alpha = from.isAlphaPremultiplied();
                        WritableRaster raster = from.copyData(null);
                        master = new BufferedImage(model, raster, alpha, null);
                    }
                } else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null) {
                    if (!hasBackround || frameIndex > 1) {
                        master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
                    }
                }
                master.createGraphics().drawImage(image, x, y, null);

                lastx = x;
                lasty = y;
            }

            {
                BufferedImage copy;

                {
                    ColorModel model = master.getColorModel();
                    boolean alpha = master.isAlphaPremultiplied();
                    WritableRaster raster = master.copyData(null);
                    copy = new BufferedImage(model, raster, alpha, null);
                }
                frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
            }

            master.flush();
        }
        reader.dispose();

        return frames.toArray(new ImageFrame[frames.size()]);
    }
}
