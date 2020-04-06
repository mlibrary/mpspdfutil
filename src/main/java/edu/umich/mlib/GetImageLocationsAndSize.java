package edu.umich.mlib;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;

import javax.imageio.ImageIO;

public class GetImageLocationsAndSize extends PDFStreamEngine {

    List<ImageInfo> imageList;

    public GetImageLocationsAndSize() throws IOException
    {
        // preparing PDFStreamEngine
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());

        imageList = new ArrayList<>();
    }

    public static void main(String[] args)throws IOException {

        PDDocument document = null;
        String fileName = args[0];
        try
        {
            document = PDDocument.load( new File(fileName) );
            /*
            int pageNum = 0;
            for( PDPage page : document.getPages() )
            {
                pageNum++;
                System.out.println( "\n\nProcessing PDF page: " + pageNum +"\n---------------------------------");
                printer.processPage(page);
                break;
            }
            */
            System.out.println( "\n\nProcessing PDF page: " + 1 +"\n---------------------------------");
            PDPage coverPage = document.getPage(0);

            GetImageLocationsAndSize printer = new GetImageLocationsAndSize();
            printer.processPage(coverPage);
            printer.processImages(coverPage);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if( document != null ) {
                document.close();
            }
        }
    }

    protected void processOperator( Operator operator, List<COSBase>operands)
            throws IOException
    {
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName) operands.get( 0 );
            // get the PDF object
            PDResources resources = getResources();
            PDXObject xobject = getResources().getXObject( objectName );

            // check if the object is an image object
            //if( xobject instanceof PDImageXObject)
            if (resources.isImageXObject(objectName))
            {
                PDImageXObject image = (PDImageXObject)xobject;
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                ImageInfo info = new ImageInfo(objectName, image, ctmNew);
                imageList.add(info);
            }
            else if(xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else
        {
            super.processOperator( operator, operands );
        }
    }

    protected void processImages(
            PDPage coverPage
            ) throws Exception
    {
        Map<Float, ImageInfoList> dList = getCoverDimension(coverPage);
        List<Float> klist = new ArrayList<>(dList.keySet());
        Collections.sort(klist);
        System.out.println("Count : " + klist.size());

        int totalWidth = 0;
         int totalHeight = 0;
        /*
        int maxHeight = 0;
        int maxWidth = 0;
        */
        int rowCtr = 0;
        for (Float f : klist) {
            rowCtr += 1;

            ImageInfoList iilist = dList.get(f);
            int mh = iilist.getMaxHeight();
            int mw = iilist.getMaxWidth();
            totalHeight += mh;

            int th = iilist.getTotalHeight();
            int tw = iilist.getTotalWidth();

            List<ImageInfo> list = iilist.toList();
            System.out.printf("%02d X: %f Count: %d MaxWidth: %d MaxHeight: %d", rowCtr, f, list.size(), mw, mh);

            Collections.sort(list);
            for (ImageInfo info : list) {
                Matrix ctmNew = info.getTransform();
                System.out.print(" " + ctmNew.getTranslateY());
                System.out.print(" " + info.getImage().getWidth() + "x" + info.getImage().getHeight());
            }
            System.out.println();
        }
        System.out.printf("Total Width: %d Total Height: %d\n", totalWidth, totalHeight);

        /*
        for (ImageInfo info : imageList) {
            //printImageInfo(info);
        }
        //Collections.sort(imageList);
        //System.out.println("+++++++++ Sorted +++++++++++++++++");

        //BufferedImage newImage = new BufferedImage(totalHeight, totalHeight, BufferedImage.TYPE_INT_ARGB);
        */
    }

    protected Map<Float, ImageInfoList> getCoverDimension(
            PDPage coverPage
        ) throws Exception
    {
        Map<Float,ImageInfoList> x2dim = new HashMap<>();
        for (ImageInfo info : imageList) {
            float x = info.getTransform().getTranslateX();
            ImageInfoList list;
            if (x2dim.containsKey(x)) {
                list = x2dim.get(x);
            } else {
                list = new ImageInfoList();
                x2dim.put(x, list);
            }
            if (info.getImage().getHeight() < 300)
                list.add(info);
        }
        return x2dim;
    }

    protected void saveImage(
            ImageInfo info
            ) throws Exception
    {
        BufferedImage image = info.getImage().getImage();

        int ndx = 0;
        File outputFile = null;
        String baseName = info.getObjectName().getName();
        do {
            baseName = String.format("%s_%d.bmp", info.getObjectName().getName(), ++ndx);
            outputFile = new File("C:\\Users\\tbelc\\Documents\\jira\\tickets\\HELIO-3281\\covers\\coordinates_test1\\coverimages",
                    baseName);

        } while (outputFile.exists());

        System.out.printf("Extracting image %s %dx%d\n",
                outputFile.getName(), image.getHeight(), image.getHeight());
        ImageIO.write(image, "BMP", outputFile);
    }

    protected void printImageInfo(
            ImageInfo info
            )
    {
        COSName objectName = info.getObjectName();
        PDImageXObject image = info.getImage();
        Matrix ctmNew = info.getTransform();

        int imageHeight = image.getHeight();

        System.out.println("\nImage [" + objectName + "]");

        float imageXScale = ctmNew.getScalingFactorX();
        float imageYScale = ctmNew.getScalingFactorY();

        // position of image in the PDF in terms of user space units
        System.out.println("position in PDF = " + ctmNew.getTranslateX() + "," + ctmNew.getTranslateY() + " in user space units");

        Point2D.Float pt = transformedPoint(ctmNew.getTranslateX(), ctmNew.getTranslateY());
        System.out.println("position in PDF = " + pt.getX() + "," + pt.getY() + " in points?");

        // raw size in pixels
        System.out.println("raw image size  = " + imageHeight + "x" + imageHeight + " in pixels");

        // displayed size in user space units
        System.out.println("displayed size  = " + imageXScale + "," + imageYScale + " in user space units");
    }

    public class ImageInfo implements Comparable<ImageInfo>
    {
        private COSName m_objectName;
        private PDImageXObject m_image;
        private Matrix m_transform;

        public ImageInfo(
                COSName objectName,
                PDImageXObject image,
                Matrix transform
                )
        {
            m_objectName = objectName;
            m_image = image;
            m_transform = transform;
        }

        public COSName getObjectName() { return m_objectName; }
        public PDImageXObject getImage() { return m_image; }
        public Matrix getTransform() { return m_transform; }

        @Override
        public int compareTo(ImageInfo info)
        {
            float ax = this.getTransform().getTranslateX();
            float bx = info.getTransform().getTranslateX();

            if (ax == bx) {
                String an = this.getObjectName().getName();
                String bn = info.getObjectName().getName();

                //int nc = an.compareTo(bn);
                int nc = 0;
                if (nc == 0) {
                    float ay = this.getTransform().getTranslateY();
                    float by = info.getTransform().getTranslateY();

                    return ay < by ? -1 : ay == by ? 0 : 1;
                }
                return nc;
            }
            return ax < bx ? -1 : 1;
        }
    }

    public class ImageInfoList
    {
        List<ImageInfo> imageList;

        public ImageInfoList()
        {
            imageList = new ArrayList<>();
        }

        public void add(
                ImageInfo info
                )
        {
            imageList.add(info);
        }

        public int getTotalHeight()
        {
            int totalHeight = 0;
            for (ImageInfo info : imageList) {
                totalHeight += info.getImage().getHeight();
            }
            return totalHeight;
        }

        public int getMaxHeight()
        {
            int maxHeight = 0;
            for (ImageInfo info : imageList) {
                maxHeight = Math.max(maxHeight, info.getImage().getHeight());
            }
            return maxHeight;
        }

        public int getTotalWidth()
        {
            int totalWidth = 0;
            for (ImageInfo info : imageList) {
                totalWidth += info.getImage().getWidth();
            }
            return totalWidth;
        }

        public int getMaxWidth()
        {
            int maxWidth = 0;
            for (ImageInfo info : imageList) {
                maxWidth = Math.max(maxWidth, info.getImage().getWidth());
            }
            return maxWidth;
        }
        
        public List<ImageInfo> toList()
        {
            return imageList;
        }
    }
}
