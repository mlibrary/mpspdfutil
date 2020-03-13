package edu.umich.mlib;

import org.apache.commons.lang3.ArrayUtils;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.resource.ImageResource;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MPSTree extends PDFDomTree
{
    int imageCtr;
    int totalWidth;
    int totalHeight;
    boolean saveImage;
    List<BufferedImage> mergedImageList;

    public MPSTree()
            throws IOException, ParserConfigurationException
    {
        super();
        imageCtr = 0;
        totalWidth = 0;
        totalHeight = 0;
        mergedImageList = new ArrayList<>();
        saveImage = true;
    }

    @Override
    protected void startNewPage()
    {
        curpage = createPageElement();
        body.appendChild(curpage);
        imageCtr = 0;
    }

    /**
     * Creates an element that represents an image drawn at the specified coordinates in the page.
     * @param x the X coordinate of the image
     * @param y the Y coordinate of the image
     * @param width the width coordinate of the image
     * @param height the height coordinate of the image
     * @param resource the image data depending on the specified type
     * @return
     */
    @Override
    protected Element createImageElement(float x, float y, float width, float height, ImageResource resource) throws IOException
    {
        StringBuilder pstyle = new StringBuilder("position:absolute;");
        pstyle.append("left:").append(x).append(UNIT).append(';');
        pstyle.append("top:").append(y).append(UNIT).append(';');
        pstyle.append("width:").append(width).append(UNIT).append(';');
        pstyle.append("height:").append(height).append(UNIT).append(';');
        //pstyle.append("border:1px solid red;");

        Element el = doc.createElement("img");
        el.setAttribute("style", pstyle.toString());

        String imgSrc = config.getImageHandler().handleResource(resource);

        String resourceName = resource.getName();
        String resourceType = resource.getFileEnding();
        String mimetype = resource.getMimeType();
        byte[] resourceData = resource.getData();

        String outputName = String.format("Page_%05d_%05d.%s", pagecnt, ++imageCtr, resourceType);
        String outputPath = String.format("mpstree_images%s%s", File.separator, outputName);
        File outputFile = new File(outputPath);

        System.out.printf("%s: %s\n", outputName, pstyle.toString());

        ByteArrayInputStream bis = new ByteArrayInputStream(resourceData);
        BufferedImage image = ImageIO.read(bis);
        ImageIO.write(image, resourceType, outputFile);

        if (!disableImageData && !imgSrc.isEmpty())
            el.setAttribute("src", imgSrc);
        else {
            //el.setAttribute("src", "");
            el.setAttribute("src", outputPath);
        }

        if (pagecnt == 24) {
            ByteArrayInputStream mbis = new ByteArrayInputStream(resourceData);
            BufferedImage mImage = ImageIO.read(mbis);

            totalWidth = Math.max(mImage.getWidth(), totalWidth);
            totalHeight += mImage.getHeight();
            mergedImageList.add(mImage);
        }

        if (pagecnt == 25 && saveImage) {
            String mergedName = String.format("Page_%05d.%s", pagecnt, resourceType);
            //String mergedPath = String.format("mpstree_images%s%s", File.separator, mergedName);
            File mergedFile = new File(mergedName);

            BufferedImage mImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = mImage.getGraphics();
            int ht = 0;
            for (BufferedImage m : mergedImageList) {
                g.drawImage(m, 0, ht, null);
                ht += m.getHeight();
            }
            ImageIO.write(mImage, resourceType, mergedFile);
            saveImage = false;
        }

        return el;
    }
}
