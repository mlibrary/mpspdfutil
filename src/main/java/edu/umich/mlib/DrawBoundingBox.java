package edu.umich.mlib;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.util.List;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

public class DrawBoundingBox
{
    public static void main(String[] args)throws IOException {

        PDDocument document = null;
        String fileName = args[0];
        try
        {
            File inputFile = new File(fileName);
            document = PDDocument.load(inputFile);
            System.out.println( "Processing PDF page: " + 1 );
            PDPage coverPage = document.getPage(0);

            //drawBoundingBox(document, coverPage);

            BoundingBoxFinder boxFinder = new BoundingBoxFinder(coverPage);
            boxFinder.processPage(coverPage);
            Rectangle2D box = boxFinder.getBoundingBox();
            List<PDImage> imageList = boxFinder.imageList;
            System.out.println( "Image count: " + imageList.size());
            for (PDImage image : imageList) {
                System.out.printf( "Width: %d Height: %d\n", image.getWidth(), image.getHeight());
            }

             /*
            int extPos = inputFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    inputFile.getName() : inputFile.getName().substring(0, extPos);
            File outputFile = new File(inputFile.getParentFile(), baseName + "_boxed.pdf");
            System.out.println( "Saving file " + outputFile.getName());
            document.save(outputFile);
            */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if( document != null ) {
                document.close();
            }
        }
    }

    static void drawBoundingBox(PDDocument pdDocument, PDPage pdPage) throws IOException {
        BoundingBoxFinder boxFinder = new BoundingBoxFinder(pdPage);
        boxFinder.processPage(pdPage);
        Rectangle2D box = boxFinder.getBoundingBox();
        if (box != null) {
            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                canvas.setStrokingColor(Color.magenta);
                canvas.addRect((float)box.getMinX(), (float)box.getMinY(), (float)box.getWidth(), (float)box.getHeight());
                canvas.stroke();
            }
        }
    }
}
