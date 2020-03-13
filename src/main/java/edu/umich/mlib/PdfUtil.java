package edu.umich.mlib;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingType1;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class PdfUtil
{
    private enum FuncCode {
        COPY_OUTLINE,
        COVER,
        EPUB,
        EXTRACT,
        FIX_OUTLINE,
        FONTS,
        INFO,
        KAKADU,
        OBJECTS,
        OPTIMIZE,
        HAS_OUTLINE,
        CODERS,
        REPLACE,
        RESIZE,
        SHRINK,
    }

    private enum FormatType {
        BMP,
        JPEG,
        JP2000,
        PNG
    }

    private static Map<String, FuncCode> STRING2FUNC = new HashMap<>();
    static {
        STRING2FUNC.put("copy_outline", FuncCode.COPY_OUTLINE);
        STRING2FUNC.put("cover", FuncCode.COVER);
        STRING2FUNC.put("epub", FuncCode.EPUB);
        STRING2FUNC.put("extract", FuncCode.EXTRACT);
        STRING2FUNC.put("fix_outline", FuncCode.FIX_OUTLINE);
        STRING2FUNC.put("fonts", FuncCode.FONTS);
        STRING2FUNC.put("info", FuncCode.INFO);
        STRING2FUNC.put("kakadu", FuncCode.KAKADU);
        STRING2FUNC.put("objects", FuncCode.OBJECTS);
        STRING2FUNC.put("optimize", FuncCode.OPTIMIZE);
        STRING2FUNC.put("has_outline", FuncCode.HAS_OUTLINE);
        STRING2FUNC.put("coders", FuncCode.CODERS);
        STRING2FUNC.put("replace", FuncCode.REPLACE);
        STRING2FUNC.put("resize", FuncCode.RESIZE);
        STRING2FUNC.put("shrink", FuncCode.SHRINK);
    }

    private static Map<String, FormatType> STRING2FORMAT = new HashMap<>();
    static {
        STRING2FORMAT.put("bmp", FormatType.BMP);
        STRING2FORMAT.put("jpeg", FormatType.JPEG);
        STRING2FORMAT.put("jpeg2000", FormatType.JP2000);
        STRING2FORMAT.put("png", FormatType.PNG);
    }

    private static Map<FormatType, String> FORMAT2EXT = new HashMap<>();
    static {
        FORMAT2EXT.put(FormatType.BMP, "bmp");
        FORMAT2EXT.put(FormatType.JPEG, "jpg");
        FORMAT2EXT.put(FormatType.JP2000, "jp2");
        FORMAT2EXT.put(FormatType.PNG, "png");
    }

    private static Map<FormatType, String> FORMAT2TYPE = new HashMap<>();
    static {
        FORMAT2TYPE.put(FormatType.BMP, "bmp");
        FORMAT2TYPE.put(FormatType.JPEG, "jpeg");
        FORMAT2TYPE.put(FormatType.JP2000, "jpeg2000");
        FORMAT2TYPE.put(FormatType.PNG, "png");
    }

    /**
     * This is the main method.
     *
     * @param args The command line arguments.
     */
    public static void main(
            String[] args
            )
    {
        if (args.length == 0) {
            System.out.println("Usage: function args");
            System.exit(0);
        }

        String funcName = args[0];
        FuncCode funcCode = STRING2FUNC.get(funcName.toLowerCase());
        if (funcCode == null) {
            System.out.println("Error: invalid function name \"" + funcName + "\".");
            System.exit(1);
        }

        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        try {
            switch (funcCode) {
                case COPY_OUTLINE:
                    copyOutlinePDF(args);
                    break;
                case COVER:
                    coverPDF(args);
                    break;
                case EPUB:
                    epubPDF(args);
                    break;
                case EXTRACT:
                    extractPDF(args);
                    break;
                case FIX_OUTLINE:
                    fixOutline(args);
                    break;
                case FONTS:
                    extractFonts(args);
                    break;
                case INFO:
                    infoPDF(args);
                    break;
                case KAKADU:
                    kakaduImages(args);
                    break;
                case OBJECTS:
                    objectsPDF(args);
                    break;
                case OPTIMIZE:
                    optimizePDF(args);
                    break;
                case HAS_OUTLINE:
                    hasOutlinePDF(args);
                    break;
                case REPLACE:
                    replacePDF(args);
                    break;
                case CODERS:
                    dumpCoders(args);
                    break;
                case RESIZE:
                    resizePDF(args);
                    break;
                case SHRINK:
                    shrinkPDF(args);
                    break;
                default:
                    System.out.println("Function \"" + funcName + "\" not implemented.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyOutline(
            PDDocument pdfDoc,
            PDDocument pdfWebDoc,
            boolean addEntries
        ) throws Exception
    {
        PDDocumentOutline outline = pdfWebDoc.getDocumentCatalog().getDocumentOutline();
        if (outline == null) {
            System.out.println("Error: no source document outline.");
            return;
        }

        PDDocumentOutline newOutline = addEntries ?
                pdfDoc.getDocumentCatalog().getDocumentOutline() :
                null;
        if (newOutline == null) {
            System.out.println("Creating new destination document outline.");
            newOutline = new PDDocumentOutline();
            pdfDoc.getDocumentCatalog().setDocumentOutline(newOutline);
        } else {
            System.out.println("Adding entries to existing source document outline.");
        }

        PDOutlineItem item = outline.getFirstChild();
        while( item != null )
        {
            PDPageDestination pd = null;

            //System.out.printf( "Item: \"%s\"\n", item.getTitle());
            if (item.getDestination() instanceof PDPageDestination)
            {
                pd = (PDPageDestination) item.getDestination();
                //System.out.println("1,Destination page: " + (pd.retrievePageNumber() + 1));
            }
            else if (item.getDestination() instanceof PDNamedDestination)
            {
                pd = pdfWebDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) item.getDestination());
                if (pd != null)
                {
                    //System.out.println("2,Destination page: " + (pd.retrievePageNumber() + 1));
                }
            }

            if (item.getAction() instanceof PDActionGoTo)
            {
                PDActionGoTo gta = (PDActionGoTo) item.getAction();
                if (gta.getDestination() instanceof PDPageDestination)
                {
                    pd = (PDPageDestination) gta.getDestination();
                    //System.out.printf("3,Destination page: %d\n",
                    //        (pd.retrievePageNumber() + 1));
                }
                else if (gta.getDestination() instanceof PDNamedDestination)
                {
                    pd = pdfWebDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                    if (pd != null)
                    {
                        //System.out.println("4,Destination page: " + (pd.retrievePageNumber() + 1));

                    }
                }
            }
            if (pd == null) {
                throw new Exception("Error: no page destination found.");
            }

            PDOutlineItem newItem = new PDOutlineItem();
            PDActionGoTo action = new PDActionGoTo();
            PDPageXYZDestination newPd = new PDPageXYZDestination();
            PDPage page = pdfDoc.getPage(pd.retrievePageNumber());
            newPd.setPage(page);
            action.setDestination(newPd);

            newItem.setAction(action);
            newItem.setTitle(item.getTitle());
            newOutline.addLast(newItem);

            item = item.getNextSibling();
        }
    }

    private static void copyOutlinePDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();
        options.addOption("a", "add_entries", false, "Add to existing outline, if exists" );
        options.addOption("c", "cover_format", true, "Cover format [bmp|jpeg|jpeg2000|png]" );
        options.addOption("p", "cover_page", true, "Cover page [0-9]+" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("optimize [options] pdf_file [pdf_file...]", options);
            return;
        }

        FormatType coverFormatType = cmdLine.hasOption("c") ? STRING2FORMAT.get(cmdLine.getOptionValue("c")) : null;
        int coverPageNumber = Integer.parseInt(cmdLine.getOptionValue("p", "0"));
        boolean addEntries = cmdLine.hasOption("a");

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);

            PDDocument pdfDoc = PDDocument.load(pdfFile);

            if (coverPageNumber >= 0 && coverPageNumber < pdfDoc.getNumberOfPages() && coverFormatType != null) {
                // Extract the cover.
                String ext = FORMAT2EXT.get(coverFormatType);
                String outputCoverPath = String.format("%s_cover.%s", baseName, ext);
                File outputCoverFile = new File(pdfFile.getAbsoluteFile().getParentFile(), outputCoverPath);
                coverPDFImages(
                        pdfDoc,
                        coverPageNumber,
                        coverFormatType,
                        outputCoverFile
                );
            }

            File pdfWebFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName + "_web.pdf");
            if (pdfWebFile.exists()) {
                // Copy the bookmarks.
                System.out.printf("Web file \"%s\" exists, copying the bookmarks.\n", pdfWebFile.getName());
                PDDocument pdfWebDoc = PDDocument.load(pdfWebFile);
                copyOutline(pdfDoc, pdfWebDoc, addEntries);
                pdfWebDoc.close();

                // Save the modified PDF to a new name.
                String outputPDFPath = String.format("%s_outline.pdf", baseName);
                File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), outputPDFPath);
                System.out.printf("Saving file \"%s\".\n", outputPDFFile.getName());
                pdfDoc.save(outputPDFFile);
            }
            pdfDoc.close();
        }
    }

    private static void epubPDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception(String.format("Usage: %s <pdf_file> [<pdf_file>...]", params[0]));
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File outputDirFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdir();
            } else if (!outputDirFile.isDirectory()) {
                throw new Exception(String.format("Error: invalid directory path \"%s\"\n.",
                        outputDirFile.getAbsoluteFile()));
            }

            String extractType = FORMAT2TYPE.get(FormatType.JPEG);
            String extractExt = FORMAT2EXT.get(FormatType.JPEG);
            PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDoc);

            for (int j = 0; j < pdfDoc.getNumberOfPages(); j++) {
                int pageNum = j + 1;

                File outputFile = new File(outputDirFile, String.format("%08d.%s",
                        pageNum, extractExt));
                //BufferedImage image = pdfRenderer.renderImageWithDPI(j, 600);
                BufferedImage image = pdfRenderer.renderImage(j);
                ImageIO.write(image, extractType, outputFile);
            }

            pdfDoc.close();
        }
    }

    private static void coverPDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();
        options.addOption("c", "cover_format", true, "Cover format [bmp|jpeg|jpeg2000|png]" );
        options.addOption("p", "cover_page", true, "Cover page [0-9]+" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("optimize [options] pdf_file [pdf_file...]", options);
            return;
        }

        FormatType coverFormatType = cmdLine.hasOption("c") ? STRING2FORMAT.get(cmdLine.getOptionValue("c")) : FormatType.PNG;
        int coverPageNumber = Integer.parseInt(cmdLine.getOptionValue("p", "0"));

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            String extractExt = FORMAT2EXT.get(coverFormatType);
            File outputFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName + "_cover." + extractExt);

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            coverPDFImages(
                    pdfDoc,
                    coverPageNumber,
                    coverFormatType,
                    outputFile
            );

            pdfDoc.close();
        }
    }

    private static void coverPDFImages(
            PDDocument pdfDoc,
            int coverImageIndex,
            FormatType formatType,
            File outputFile
    ) throws Exception
    {
        if (pdfDoc.getNumberOfPages() == 0) {
            return;
        }

        // Instantiate the renderer.
        PDFRenderer renderer = new PDFRenderer(pdfDoc);

        // Extract the specified page as the cover.
        BufferedImage coverImage = renderer.renderImage(coverImageIndex);
        String extractType = FORMAT2TYPE.get(formatType);
        System.out.printf("Extracting cover image %s\n",
                outputFile.getName()
        );
        ImageIO.write(coverImage, extractType, outputFile);
    }

    private static void extractPDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception("Usage: extract <pdf_file> [<pdf_file>...]");
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File outputDirFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdir();
            } else if (!outputDirFile.isDirectory()) {
                throw new Exception(String.format("Error: invalid directory path \"%s\"\n.",
                        outputDirFile.getAbsoluteFile()));
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            extractPDFImages(
                    pdfDoc,
                    FormatType.JPEG,
                    outputDirFile
            );

            pdfDoc.close();
        }
    }

    private static void extractPDFImages(
            PDDocument pdfDoc,
            FormatType formatType,
            File outputDirFile
    ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();
            String extractExt = FORMAT2EXT.get(formatType);
            String extractType = FORMAT2TYPE.get(formatType);

            // Traverse source page resources.
            int imageNum = 0;
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    imageNum += 1;

                    PDImageXObject imageObj = (PDImageXObject) resources.getXObject(name);
                    BufferedImage image = imageObj.getImage();

                    File outputFile = new File(outputDirFile, String.format("Page_%04d_Image_%04d.%s",
                            pageNum, imageNum, extractExt));

                    System.out.printf("Extracting image %s %dx%d\n",
                            outputFile.getName(), image.getWidth(), image.getHeight());
                    ImageIO.write(image, extractType, outputFile);
                }
            }
        }
    }

    private static void extractFonts(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception("Usage: extract <pdf_file> [<pdf_file>...]");
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            /*
            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File outputDirFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdir();
            } else if (!outputDirFile.isDirectory()) {
                throw new Exception(String.format("Error: invalid directory path \"%s\"\n.",
                        outputDirFile.getAbsoluteFile()));
            }
            */
            PDDocument pdfDoc = PDDocument.load(pdfFile);
            for (int j = 0; j < pdfDoc.getNumberOfPages(); j++) {
                PDPage page = pdfDoc.getPage(j);
                PDResources resources = page.getResources();
                for (COSName fontName : resources.getFontNames()) {
                    PDFont font = resources.getFont(fontName);
                    System.out.printf("%s (%s): hash: %s embedded: %d standard14: %d\n",
                            fontName, font.getName(),
                            font.hashCode(),
                            font.isEmbedded() ? 1 : 0,
                            font.isStandard14() ? 1 : 0);
                }
            }
            pdfDoc.close();
        }
    }

    private static void infoPDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 1;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("info pdf_file [pdf_file...]", options);
            return;
        }

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File outputFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName + "_resources.log");

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            infoPDFImages(
                    pdfDoc,
                    outputFile
            );
            pdfDoc.close();
        }
    }

    private static void infoPDFImages(
            PDDocument pdfDoc,
            File outputFile
        ) throws Exception
    {
        List<String> summary = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        Map<String, Integer> suffixMap = new HashMap<>();

        int imageCnt = 0;
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            // Traverse source page resources get image count
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    imageCnt += 1;
                }
            }
        }
        summary.add(String.format("Count: %d\n", imageCnt));

        // Traverse the source PDF pdfDoc pages.
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            // Traverse source page resources.
            int imageNum = 0;
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    imageNum += 1;

                    PDImageXObject imageObj = (PDImageXObject) resources.getXObject(name);
                    String suffix = imageObj.getSuffix();
                    if (suffixMap.containsKey(suffix)) {
                        int cnt = suffixMap.get(suffix);
                        suffixMap.replace(suffix, cnt+1);
                    } else {
                        suffixMap.put(suffix, 1);
                    }

                    lines.add(String.format("-- Page %04d Image %04d --\n", pageNum, imageNum));
                    lines.add(String.format("Type: %s\n", suffix));
                    lines.add(String.format("Dimensions: %dx%d\n", imageObj.getWidth(), imageObj.getHeight()));
                    lines.add(String.format("Color Space: %s\n", imageObj.getColorSpace().getName()));
                    lines.add(String.format("Components: %d\n", imageObj.getColorSpace().getNumberOfComponents()));
                    lines.add(String.format("Bits/Component: %d\n", imageObj.getBitsPerComponent()));
                }
            }
        }

        for (Map.Entry<String, Integer> entry : suffixMap.entrySet()) {
            summary.add(String.format("%s: %d\n", entry.getKey(), entry.getValue()));
        }
        summary.addAll(lines);
        FileUtils.writeStringToFile(outputFile, summary.toString(), StandardCharsets.UTF_8);
    }

    private static void objectsPDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception(String.format("Usage: %s <pdf_file> [<pdf_file>...]", params[0]));
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            objectsPDFImages(
                    pdfDoc
            );
            pdfDoc.close();
        }
    }

    private static void objectsPDFImages(
            PDDocument pdfDoc
    ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        ArrayList<COSName> objectList = new ArrayList<>();

        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            // Traverse source page resources.
            int imageNum = 0;
            for (COSName name : xobjectNames) {
                System.out.printf("%s %b\n", name.toString(), resources.isImageXObject(name));
                objectList.add(name);
            }
        }

        System.out.printf("Number of objects: %d\n", objectList.size());
    }

    private static void optimizePDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();
        options.addOption("c", "cover_format", true, "Cover format [bmp|jpeg|jpeg2000|png]" );
        options.addOption("d", "delete_dir", false, "Delete image directory" );
        options.addOption("f", "image_format", true, "Image format [bmp|jpeg|jpeg2000|png]" );
        options.addOption("l", "compression_level", true, "Compression level %" );
        options.addOption("p", "cover_page", true, "Cover page [0-9]+" );
        options.addOption("r", "resize_pct", true, "Resize %" );
        options.addOption("t", "dimen_threshold", true, "Dimension threshold [0-9]+" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("optimize [options] pdf_file [pdf_file...]", options);
            return;
        }

        FormatType coverFormatType = cmdLine.hasOption("c") ? STRING2FORMAT.get(cmdLine.getOptionValue("c")) : null;
        FormatType imageFormatType = STRING2FORMAT.get(cmdLine.getOptionValue("f", "jpeg"));
        int coverPageNumber = Integer.parseInt(cmdLine.getOptionValue("p", "0"));
        int compressionLevel = Integer.parseInt(cmdLine.getOptionValue("l", "70"));
        int resizePct = Integer.parseInt(cmdLine.getOptionValue("r", "100"));
        int imageSizeLimit = Integer.parseInt(cmdLine.getOptionValue("t", "0"));

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            String outputDirPath = String.format("%s_%dpct_%dlevel", baseName, resizePct, compressionLevel);
            File outputDirFile = new File(pdfFile.getAbsoluteFile().getParentFile(), outputDirPath);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdir();
            } else if (!outputDirFile.isDirectory()) {
                throw new Exception(String.format("Error: invalid directory path \"%s\"\n.",
                        outputDirFile.getAbsoluteFile()));
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);

            if (coverPageNumber >= 0 && coverPageNumber < pdfDoc.getNumberOfPages() && coverFormatType != null) {
                // Extract the cover.
                String ext = FORMAT2EXT.get(coverFormatType);
                String outputCoverPath = String.format("%s_cover.%s", baseName, ext);
                File outputCoverFile = new File(pdfFile.getAbsoluteFile().getParentFile(), outputCoverPath);
                coverPDFImages(
                        pdfDoc,
                        coverPageNumber,
                        coverFormatType,
                        outputCoverFile
                );
            }

            // Resize the images.
            boolean result = optimizePDFImages(
                    pdfDoc,
                    imageFormatType,
                    compressionLevel,
                    resizePct,
                    imageSizeLimit,
                    outputDirFile
            );

            if (result) {
                File pdfWebFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName + "_web.pdf");
                if (pdfWebFile.exists()) {
                    // Copy the bookmarks.
                    System.out.printf("Web file \"%s\" exists, copying the bookmarks.\n", pdfWebFile.getName());
                    PDDocument pdfWebDoc = PDDocument.load(pdfWebFile);
                    copyOutline(pdfDoc, pdfWebDoc, false);
                    pdfWebDoc.close();
                }

                // Save the resized PDF to a new name.
                String outputPDFPath = String.format("%s_optimize_%dpct_%dlevel.pdf",
                        baseName, resizePct, compressionLevel);
                File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), outputPDFPath);
                System.out.printf("Saving file \"%s\".", outputPDFFile.getName());
                pdfDoc.save(outputPDFFile);
            } else {
                System.out.printf("File \"%s\" has not been reduced.\n", pdfFile.getName());
            }

            pdfDoc.close();

            if (cmdLine.hasOption("d")) {
                FileUtils.deleteDirectory(outputDirFile);
            }
        }
    }

    private static boolean optimizePDFImages(
            PDDocument pdfDoc,
            FormatType formatType,
            int compressionLevel,
            int resizePct,
            int imageSizeLimit,
            File outputDirFile
        ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            String extractExt = FORMAT2EXT.get(formatType);
            String extractType = FORMAT2TYPE.get(formatType);

            // Traverse source page resources.
            int imageNum = 0;
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    imageNum += 1;

                    File outputFile = new File(outputDirFile, String.format("Page_%04d_Image_%04d.%s",
                            pageNum, imageNum, extractExt));

                    PDImageXObject imageObj = (PDImageXObject) resources.getXObject(name);
                    String suffix = imageObj.getSuffix();

                    /*
                    if (resizePct == 100 && suffix.equalsIgnoreCase("jpg")) {
                        System.out.printf("Skipping image page %d image %d suffix %s resizePct %d\n",
                                pageNum, imageNum, suffix, resizePct);
                        continue;
                    }
                    */
                    if (!suffix.equalsIgnoreCase("jpg") && !suffix.equalsIgnoreCase("png") && !suffix.equalsIgnoreCase("bmp")) {
                        System.out.printf("Skipping image page %d image %d suffix %s\n",
                                pageNum, imageNum, suffix);
                        continue;
                    }
                    System.out.printf("Processing image page %d image %d suffix %s\n",
                            pageNum, imageNum, suffix);

                    BufferedImage image = null;
                    try {
                        image = imageObj.getImage();
                    } catch (MissingImageReaderException exception) {
                        System.out.printf("Error: %s\n", exception.getLocalizedMessage());
                        //return false;
                        continue;
                    }

                    BufferedImage scaledImage = image;
                    boolean imageScaled = false;

                    int width = image.getWidth();
                    int height = image.getHeight();
                    if (width < imageSizeLimit && height < imageSizeLimit) {
                        System.out.printf("Dimensions: %dx%d less than limit %d. Skipping scaling.\n",
                                width, height, imageSizeLimit);
                    } else if (resizePct == 100) {
                        System.out.printf("Resize pct = %d. Skipping scaling.\n", resizePct);
                    } else {
                        Dimension imageDim = new Dimension(width, height);
                        int newWidth = ((width * resizePct) / 100);
                        newWidth = newWidth < 1 ? 1 : newWidth;
                        int newHeight = ((height * resizePct) / 100);
                        newHeight = newHeight < 1 ? 1 : newHeight;
                        Dimension newImageDim = new Dimension(newWidth, newHeight);
                        System.out.printf("Dimensions: %dx%d => %dx%d\n", width, height,
                                newWidth, newHeight);

                        Dimension newDim = getScaledDimension(imageDim, newImageDim);
                        newWidth = newDim.getWidth() < 1.0 ? 1 : (int) newDim.getWidth();
                        newHeight = newDim.getHeight() < 1.0 ? 1 : (int) newDim.getHeight();
                        scaledImage = getScaledInstance(
                                image,
                                newWidth,
                                newHeight,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                                true);
                        imageScaled = true;
                    }

                    System.out.printf("Optimizing image %s %dx%d\n",
                            outputFile.getName(), scaledImage.getWidth(), scaledImage.getHeight());

                    // Can't write this as a jpeg2000 because
                    // PDImageXObject.createFromFile below will
                    // throw an exception attempting to read a jp2 file.
                    // Using ImageIO.read will load the jp2 image, but
                    // LosslessFactory.createFromImage will convert it to a
                    // PNG and insert it into the PDF as such, thus no file
                    // size savings.
                    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
                    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    jpegParams.setCompressionQuality(compressionLevel / 100.0f);

                    FileImageOutputStream outStream = new FileImageOutputStream(outputFile);
                    final ImageWriter writer = ImageIO.getImageWritersBySuffix(extractExt).next();
                    writer.setOutput(outStream);

                    try {
                        writer.write(null, new IIOImage(scaledImage, null, null), jpegParams);
                    } catch (IIOException exception) {
                        // Problem writing image. Log a message and skip it.
                        System.out.printf("Optimizing image %s %dx%d write FAILED, not replaced.\n",
                                outputFile.getName(), scaledImage.getWidth(), scaledImage.getHeight());
                        continue;
                    } finally {
                        outStream.close();
                        writer.dispose();
                    }

                    /*
                    ImageInputStream stream = ImageIO.createImageInputStream(outputFile);
                    try {
                        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                        BufferedImage newImage = null;
                        while (readers.hasNext()) {
                            ImageReader reader = readers.next();
                            reader.setInput(stream);

                            try {
                                newImage = reader.read(0);
                            } catch (IIOException exception) {
                                System.out.printf("Optimizing image %s %dx%d re-read FAILED, not replaced.\n",
                                        outputFile.getName(), scaledImage.getWidth(), scaledImage.getHeight());
                                continue;
                            } finally {
                                reader.dispose();
                            }
                        }
                        //BufferedImage newImage = ImageIO.read(outputFile);
                        PDImageXObject newObj = LosslessFactory.createFromImage(pdfDoc, newImage);
                        resources.put(name, newObj);
                    } finally {
                        stream.close();
                    }
                        */
                    PDImageXObject newObj = PDImageXObject.createFromFile(outputFile.getAbsolutePath(), pdfDoc);
                    resources.put(name, newObj);
                }
            }
        }
        return true;
    }

    private static void fixOutline(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 1;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(String.format("%s [options] pdf_file [pdf_file...]", params[0]), options);
            return;
        }

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                System.out.printf("Error: invalid file path \"%s\".\n", pdfFileName);
                continue;
            }
            System.out.printf("Fixing \"%s\".\n", pdfFileName);

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDDocumentCatalog catalog = pdfDoc.getDocumentCatalog();
            PDDocumentOutline outline = catalog.getDocumentOutline();
            if (outline == null) {
                System.out.printf("\"%s\": Warning: document outline does not exist.\n", pdfFile.getName());
            } else {
                PDDocumentOutline newOutline = new PDDocumentOutline();
                catalog.setDocumentOutline(newOutline);

                PDOutlineItem item = outline.getFirstChild();
                while( item != null )
                {
                    PDPageDestination pd = null;
                    String title = item.getTitle().trim();

                    if (item.getDestination() instanceof PDPageDestination)
                    {
                        pd = (PDPageDestination) item.getDestination();
                    }
                    else if (item.getDestination() instanceof PDNamedDestination)
                    {
                        pd = pdfDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) item.getDestination());
                    }

                    if (item.getAction() instanceof PDActionGoTo)
                    {
                        PDActionGoTo gta = (PDActionGoTo) item.getAction();
                        if (gta.getDestination() instanceof PDPageDestination)
                        {
                            pd = (PDPageDestination) gta.getDestination();
                        }
                        else if (gta.getDestination() instanceof PDNamedDestination)
                        {
                            pd = pdfDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                        }
                    }
                    if (pd == null) {
                        System.out.printf("Removing bookmark \"%s\"\n", title);
                    } else {
                        int pgnum = pd.retrievePageNumber();
                        if (pgnum >= 0) {
                            System.out.printf("Adding bookmark \"%s\"\n", title);
                            PDOutlineItem newItem = new PDOutlineItem();
                            PDActionGoTo action = new PDActionGoTo();
                            PDPageXYZDestination newPd = new PDPageXYZDestination();
                            PDPage page = pdfDoc.getPage(pd.retrievePageNumber());
                            newPd.setPage(page);
                            action.setDestination(newPd);

                            newItem.setAction(action);
                            newItem.setTitle(item.getTitle());
                            newOutline.addLast(newItem);
                        } else {
                            System.out.printf("Error: invalid page number %d. Removing bookmark \"%s\"\n", pgnum, title);
                        }
                    }
                    item = item.getNextSibling();
                }
            }

            // Save the modified PDF to a new name.

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            String outputPDFPath = String.format("%s_fix.pdf", baseName);
            File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), outputPDFPath);
            System.out.printf("Saving file \"%s\".\n", outputPDFFile.getName());
            pdfDoc.save(outputPDFFile);

            pdfDoc.close();
        }
    }

    private static void hasOutlinePDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();
        options.addOption("v", "verbose", false, "Display outline" );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 1;
        if (params.length > 1) {
            try {
                cmdLine = parser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("has_outline [options] pdf_file [pdf_file...]", options);
            return;
        }

        boolean displayOutline = cmdLine.hasOption("v");

        List<String> pdfFileList = cmdLine.getArgList();
        for (String pdfFileName : pdfFileList.subList(1, pdfFileList.size())) {
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                System.out.printf("Error: invalid file path \"%s\".\n", pdfFileName);
                continue;
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDDocumentOutline outline = pdfDoc.getDocumentCatalog().getDocumentOutline();
            if (outline == null) {
                System.out.printf("\"%s\": Warning: document outline does not exist.\n", pdfFile.getName());
            } else {

                List<String> msgList = new ArrayList<String>();
                List<String> errList = new ArrayList<String>();

                PDOutlineItem item = outline.getFirstChild();
                int itemCnt = 0, errCnt = 0;
                while( item != null )
                {
                    PDPageDestination pd = null;
                    String title = item.getTitle().trim();
                    itemCnt += 1;

                    if (item.getDestination() instanceof PDPageDestination)
                    {
                        pd = (PDPageDestination) item.getDestination();
                    }
                    else if (item.getDestination() instanceof PDNamedDestination)
                    {
                        pd = pdfDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) item.getDestination());
                    }

                    if (item.getAction() instanceof PDActionGoTo)
                    {
                        PDActionGoTo gta = (PDActionGoTo) item.getAction();
                        if (gta.getDestination() instanceof PDPageDestination)
                        {
                            pd = (PDPageDestination) gta.getDestination();
                        }
                        else if (gta.getDestination() instanceof PDNamedDestination)
                        {
                            pd = pdfDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                        }
                    }
                    if (pd == null) {
                        errCnt += 1;
                        String msg = String.format("Error: title \"%s\" has no page destination.", title);
                        msgList.add(msg);
                        errList.add(msg);
                        //System.out.printf("\tError: title \"%s\" has no page destination.\n", title);
                    } else if (displayOutline) {
                        String msg = String.format("Page %d: \"%s\"", (pd.retrievePageNumber() + 1), title);
                        msgList.add(msg);
                        //System.out.printf("\tPage %d: \"%s\"\n", (pd.retrievePageNumber() + 1), title);
                    }

                    item = item.getNextSibling();
                }
                System.out.printf("\"%s\" total bookmarks: %d  errors: %d\n", pdfFile.getName(), itemCnt, errCnt);
                if (displayOutline) {
                    for (String msg : msgList) {
                        System.out.printf("\t%s\n", msg);
                    }
                } else {
                    for (String msg : errList) {
                        System.out.printf("\t%s\n", msg);
                    }
                }
            }
            pdfDoc.close();
        }
    }

    private static void replacePDF(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception("Usage: replace <pdf_file> [<pdf_file>...]");
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File inputDirFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName);
            if (!inputDirFile.exists() || !inputDirFile.isDirectory()) {
                throw new Exception(String.format("Error: invalid directory path \"%s\"\n.",
                        inputDirFile.getAbsoluteFile()));
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            replacePDFImages(pdfDoc, inputDirFile);

            File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), baseName + "_new.pdf");
            pdfDoc.save(outputPDFFile);
            pdfDoc.close();
        }
    }

    private static void replacePDFImages(
            PDDocument pdfDoc,
            File inputDirFile
        ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            PDPage page = pdfDoc.getPage(i);

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            // Traverse source page resources.
            int imageNum = 0;
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    imageNum += 1;

                    File inputFile = new File(inputDirFile,
                            String.format("Page_%04d_Image_%04d.jpg", pageNum, imageNum));
                    if (!inputFile.exists()) {
                        System.out.printf("Image \"%s\" does not exist. Skipping.", inputFile.getName());
                        continue;
                    }

                    System.out.printf("Replacing image \"%s\"\n", inputFile.getName());
                    PDImageXObject newObj = PDImageXObject.createFromFile(inputFile.getAbsolutePath(), pdfDoc);
                    //BufferedImage image = ImageIO.read(inputFile);
                    //PDImageXObject newObj = LosslessFactory.createFromImage(pdfDoc, image);
                    resources.put(name, newObj);
                }
            }
        }
    }

    private static void resizePDF(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 3) {
            throw new Exception("Usage: resize <resizePct> <pdf_file> [<pdf_file>...]");
        }

        int resizePct = Integer.parseInt(params[1]);
        if (resizePct <= 0) {
            throw new Exception(String.format("Error: invalid resize percentage \"%s\".", params[1]));
        }

        for (int i = 2; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                System.out.printf("Error: invalid file path \"%s\".", pdfFileName);
                return;
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            resizePDFImages(pdfDoc, resizePct);

            // Save the new PDF and close it.
            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            String outputPDFPath = String.format("%s_resize_%d.pdf", baseName, resizePct);
            File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), outputPDFPath);
            pdfDoc.save(outputPDFFile);
            pdfDoc.close();
        }
    }

    private static void resizePDFImages(
            PDDocument pdfDoc,
            int resizePct
        ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        for (int i = 0; i < pdfDoc.getNumberOfPages(); i++) {
            int pageNum = i + 1;

            System.out.println("*** Page " + pageNum + " ***");
            PDPage page = pdfDoc.getPage(i);

            if (pageNum >= 15) {
                // For testing, just do a subset of pages.
                break;
            }

            // Get the page resources.
            PDResources resources = page.getResources();
            Iterable<COSName> xobjectNames = resources.getXObjectNames();

            // Traverse source page resources.
            for (COSName name : xobjectNames) {
                if (resources.isImageXObject(name)) {
                    PDImageXObject imageObj = (PDImageXObject) resources.getXObject(name);
                    BufferedImage image = imageObj.getImage();
                    int width = image.getWidth();
                    int height = image.getHeight();
                    Dimension imageDim = new Dimension(width, height);
                    int newWidth = ((width * resizePct)/100);
                    int newHeight = ((height * resizePct)/100);
                    Dimension newImageDim = new Dimension(newWidth, newHeight);
                    System.out.printf("Dimensions: %dx%d => %dx%d\n", width, height,
                            newWidth, newHeight);

                    Dimension newDim = getScaledDimension(imageDim, newImageDim);
                    BufferedImage scaledImage = getScaledInstance(
                            image,
                            (int) newDim.getWidth(),
                            (int) newDim.getHeight(),
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                            true);

                    PDImageXObject resizedXobject = LosslessFactory.createFromImage(pdfDoc, scaledImage);
                    resources.put(name, resizedXobject);
                }
            }
        }
    }

    private static void shrinkPDF(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception(String.format("Usage: %s <pdf_file> [<pdf_file>...]", params[0]));
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                System.out.printf("Error: invalid file path \"%s\".", pdfFileName);
                return;
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            shrinkPDFImages(pdfDoc);

            // Save the new PDF and close it.
            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            String outputPDFPath = String.format("%s_%s.pdf", baseName, params[0]);
            File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), outputPDFPath);
            pdfDoc.save(outputPDFFile);
            pdfDoc.close();
        }
    }
    private static void shrinkPDFImages(
            PDDocument pdfDoc
        ) throws Exception
    {
        // Traverse the source PDF pdfDoc pages.
        for (PDPage page : pdfDoc.getPages())
        {
            byte[] ba;
            try (InputStream contents = page.getContents())
            {
                ba = IOUtils.toByteArray(contents);
            }
            page.setContents(new PDStream(pdfDoc, new ByteArrayInputStream(ba)));
            if (page.getAnnotations().isEmpty())
            {
                page.setAnnotations(null);
            }
            processContent(page);
            if (page.getCOSObject().containsKey(COSName.ROTATE) && page.getRotation() == 0)
            {
                page.getCOSObject().removeItem(COSName.ROTATE);
            }
            // PDRectangle doesn't have equals()
            if (page.getCOSObject().containsKey(COSName.CROP_BOX) && page.getCropBox().toString().equals(page.getMediaBox().toString()))
            {
                page.getCOSObject().removeItem(COSName.CROP_BOX);
            }
        }
        PDAcroForm acroForm = pdfDoc.getDocumentCatalog().getAcroForm();
        if (acroForm != null && !acroForm.hasXFA() && !acroForm.getFieldIterator().hasNext())
        {
            pdfDoc.getDocumentCatalog().setAcroForm(null);
        }
        if (pdfDoc.getVersion() < 1.5)
        {
            pdfDoc.setVersion(1.5f);
        }
        pdfDoc.getDocument().setIsXRefStream(true); // requires 1.5 or higher.

        pdfDoc.getDocumentCatalog().setMetadata(null);

        // don't do this if your PDF has tags e.g. for vision impaired people
        //pdfDoc.getDocumentCatalog().setStructureTreeRoot(null);
    }

    private static void processContent(PDContentStream content) throws IOException {
        Set<COSName> usedFontNames = new HashSet<>();
        //InputStream is = content.getContents();
        PDFStreamParser parser = new PDFStreamParser(content);
        List<COSBase> arguments = new ArrayList<>();
        Object token = parser.parseNextToken();
        while (token != null) {
            if (token instanceof COSObject) {
                arguments.add(((COSObject) token).getObject());
            } else if (token instanceof Operator) {
                String opname = ((Operator) token).getName();
                if ("Tf".equals(opname)) {
                    usedFontNames.add((COSName) arguments.get(0));
                }
                arguments = new ArrayList<>();
            } else {
                arguments.add((COSBase) token);
            }
            token = parser.parseNextToken();
        }
        //is.close();

        PDResources resources = content.getResources();
        if (resources == null) {
            return;
        }

        // What font names are not used?
        Set<COSName> unusedFontNames = new HashSet<>();
        for (COSName name : resources.getFontNames()) {
            if (!usedFontNames.contains(name)) {
                unusedFontNames.add(name);
            } else {
                PDFont font = resources.getFont(name);
                if (font instanceof PDType1Font) {
                    PDType1Font type1Font = (PDType1Font) font;
                    if (type1Font.isStandard14()) {
                        if (PDType1Font.HELVETICA.getName().equals(type1Font.getName())) {
                            resources.put(name, PDType1Font.HELVETICA);
                        }
                        if (PDType1Font.COURIER.getName().equals(type1Font.getName())) {
                            resources.put(name, PDType1Font.COURIER);
                        }
                        if (PDType1Font.ZAPF_DINGBATS.getName().equals(type1Font.getName())) {
                            resources.put(name, PDType1Font.ZAPF_DINGBATS);
                        }
                        // ... more (bold, etc)
                    }
                }
            }
        }
        COSDictionary fontDictionary = (COSDictionary) resources.getCOSObject().getDictionaryObject(COSName.FONT);
        if (fontDictionary != null) {
            for (COSName name : unusedFontNames) {
                fontDictionary.removeItem(name);
            }
        }

        resources.getCOSObject().removeItem(COSName.PROC_SET);

        for (COSName name : resources.getXObjectNames()) {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                processContent(form);
                if (form.getCOSObject().containsKey(COSName.MATRIX) && form.getMatrix().equals(new Matrix())) {
                    form.getCOSObject().removeItem(COSName.MATRIX);
                }
            }
        }
        for (COSName name : resources.getPatternNames()) {
            PDAbstractPattern pattern = resources.getPattern(name);
            if (pattern instanceof PDTilingPattern) {
                PDTilingPattern tilingPattern = (PDTilingPattern) pattern;
                processContent(tilingPattern);
            }
            if (pattern.getCOSObject().containsKey(COSName.MATRIX) && pattern.getMatrix().equals(new Matrix())) {
                pattern.getCOSObject().removeItem(COSName.MATRIX);
            }
        }
        for (COSName name : resources.getShadingNames()) {
            PDShading shading = resources.getShading(name);
            if (shading instanceof PDShadingType1) {
                PDShadingType1 type1Shading = (PDShadingType1) shading;
                if (type1Shading.getCOSObject().containsKey(COSName.MATRIX) && type1Shading.getMatrix().equals(new Matrix())) {
                    type1Shading.getCOSObject().removeItem(COSName.MATRIX);
                }
            }
        }
        for (COSName name : resources.getExtGStateNames()) {
            PDExtendedGraphicsState extGState = resources.getExtGState(name);
            PDSoftMask softMask = extGState.getSoftMask();
            if (softMask != null) {
                PDTransparencyGroup group = softMask.getGroup();
                if (group != null) {
                    processContent(group);
                    if (group.getCOSObject().containsKey(COSName.MATRIX) && group.getMatrix().equals(new Matrix())) {
                        group.getCOSObject().removeItem(COSName.MATRIX);
                    }
                }
            }
        }
    }

    private static void kakaduImages(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception("Usage: kakadu <bmp_file> [<bmp_file>...]");
        }

        //String commandStr = "kdu_compress -i \"%s\" -o \"%s\" Clevels=%d Clayers=8 Corder=RLCP Cuse_sop=yes Cuse_eph=yes Cmodes='RESET|RESTART|CAUSAL|ERTERM|SEGMARK' -no_weights -slope 42988";
        String commandStr = "kdu_compress -i \"%s\" -o \"%s\" Clevels=%d Clayers=8 Corder=RLCP Cuse_sop=yes Cuse_eph=yes Cmodes=\"RESET|RESTART|CAUSAL|ERTERM|SEGMARK\" -no_weights -slope 42988";

        for (int i = 1; i < params.length; i++) {
            String bmpFileName = params[i];
            File bmpFile = new File(bmpFileName);
            if (!bmpFile.exists()) {
                System.out.printf("Error: invalid file path \"%s\". Skipping.", bmpFileName);
                continue;
            }

            BufferedImage image = ImageIO.read(bmpFile);

            String fileName = bmpFile.getName();
            if (fileName.indexOf(".") > 0)
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            File newFile = new File(bmpFile.getParentFile(), fileName + ".jp2");

            int width = image.getWidth();
            int height = image.getHeight();
            //double levels = (Math.max(5, Math.ceil(log2(Math.max(width, height) / 100.0))-1));
            double dimLevel = Math.ceil(log2(Math.max(width, height) / 100.0)) - 1;
            int level = (int)(Math.max(5.0, dimLevel));
            System.out.printf("%s => %s: levels: %d width: %d height: %d\n",
                    bmpFile.getName(), newFile.getName(), level, width, height);

            String command = String.format(commandStr, bmpFile.getAbsolutePath(), newFile.getAbsolutePath(), level);
            System.out.println(command);

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }
            errorReader.close();
        }
    }

    private static double log2(double f)
    {
        //return (int)Math.floor(Math.log(f)/Math.log(2.0));
        return Math.log(f)/Math.log(2.0);
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }

    private static BufferedImage getScaledInstance(BufferedImage img,
                                                  int targetWidth,
                                                  int targetHeight,
                                                  Object hint,
                                                  boolean higherQuality)
    {
        //int type = (img.getTransparency() == Transparency.OPAQUE) ?
        //        BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        //int type = BufferedImage.TYPE_BYTE_BINARY;
        int type = img.getType();
        type = type == 0 ? BufferedImage.TYPE_4BYTE_ABGR_PRE : type;

        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    private static void dumpCoders(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 1) {
            throw new Exception(String.format("Usage: %s", params[0]));
        }

        /*
        System.out.println("Reader suffixes:");
        String[] readerFileSuffixes = ImageIO.getReaderFileSuffixes();
        for (String suffix : readerFileSuffixes) {
            System.out.println(suffix);
        }

        System.out.println("Writer suffixes:");
        String[] writerFileSuffixes = ImageIO.getWriterFileSuffixes();
        for (String suffix : writerFileSuffixes) {
            System.out.println(suffix);
        }
        */

        System.out.println("Writer formats:");
        String[] writerFormats = ImageIO.getWriterFormatNames();
        for (String format : writerFormats) {
            System.out.println(format);
        }
    }

    private static void extractPDFOutline(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);
        if (params.length < 2) {
            throw new Exception(String.format("Usage: %s <pdf_file> [<pdf_file>...]", params[0]));
        }

        for (int i = 1; i < params.length; i++) {
            String pdfFileName = params[i];
            File pdfFile = new File(pdfFileName);
            if (!pdfFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);
            File pdfWebFile = new File(pdfFile.getAbsoluteFile().getParentFile(), baseName + "_web.pdf");
            if (!pdfWebFile.exists()) {
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfWebFile.getAbsolutePath()));
            }

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDDocument pdfWebDoc = PDDocument.load(pdfWebFile);

            PDDocumentOutline outline = pdfWebDoc.getDocumentCatalog().getDocumentOutline();
            if (outline == null) {
                throw new Exception(String.format("Error: no document outline in \"%s\".\n.", pdfWebFile.getName()));
            }

            PDDocumentOutline newOutline = pdfDoc.getDocumentCatalog().getDocumentOutline();
            if (newOutline == null) {
                //newOutline = new PDDocumentOutline(pdfDoc.getDocument().getEncryptionDictionary());
                newOutline = new PDDocumentOutline();
                pdfDoc.getDocumentCatalog().setDocumentOutline(newOutline);
                System.out.printf("Created new document outline for \"%s\".\n", pdfFile.getName());
            }

            PDOutlineItem item = outline.getFirstChild();
            while( item != null )
            {
                PDPageDestination pd = null;

                System.out.printf( "Item: \"%s\"\n", item.getTitle());
                if (item.getDestination() instanceof PDPageDestination)
                {
                    pd = (PDPageDestination) item.getDestination();
                    System.out.println("1,Destination page: " + (pd.retrievePageNumber() + 1));
                }
                else if (item.getDestination() instanceof PDNamedDestination)
                {
                    pd = pdfWebDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) item.getDestination());
                    if (pd != null)
                    {
                        System.out.println("2,Destination page: " + (pd.retrievePageNumber() + 1));
                    }
                }

                if (item.getAction() instanceof PDActionGoTo)
                {
                    PDActionGoTo gta = (PDActionGoTo) item.getAction();
                    if (gta.getDestination() instanceof PDPageDestination)
                    {
                        pd = (PDPageDestination) gta.getDestination();
                        System.out.printf("3,Destination page: %d\n",
                                (pd.retrievePageNumber() + 1));
                    }
                    else if (gta.getDestination() instanceof PDNamedDestination)
                    {
                        pd = pdfWebDoc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) gta.getDestination());
                        if (pd != null)
                        {
                            System.out.println("4,Destination page: " + (pd.retrievePageNumber() + 1));

                        }
                    }
                }
                if (pd == null) {
                    throw new Exception("Error: no page destination found.");
                }

                PDOutlineItem newItem = new PDOutlineItem();
                PDActionGoTo action = new PDActionGoTo();
                PDPageXYZDestination newPd = new PDPageXYZDestination();
                PDPage page = pdfDoc.getPage(pd.retrievePageNumber());
                newPd.setPage(page);
                action.setDestination(newPd);

                newItem.setAction(action);
                newItem.setTitle(item.getTitle());
                newOutline.addLast(newItem);

                item = item.getNextSibling();
            }

            File outputPDFFile = new File(pdfFile.getAbsoluteFile().getParent(), baseName + "_outline.pdf");
            pdfDoc.save(outputPDFFile);

            pdfWebDoc.close();
            pdfDoc.close();
        }
    }
}
