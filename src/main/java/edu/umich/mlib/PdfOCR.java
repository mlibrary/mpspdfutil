package edu.umich.mlib;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.cli.*;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.apache.pdfbox.io.RandomAccessFile;
import org.fit.pdfdom.PDFDomTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.util.*;
import java.util.List;

public class PdfOCR
{
    private enum FuncCode {
        MPSTREE,
        OCR,
        PDFDOM,
        PDFDOMHTML,
        PDFHTML,
        PDFTEXT
    }

    private static Map<String, FuncCode> STRING2FUNC = new HashMap<>();
    static {
        STRING2FUNC.put("mpstree", FuncCode.MPSTREE);
        STRING2FUNC.put("ocr", FuncCode.OCR);
        STRING2FUNC.put("pdfdom", FuncCode.PDFDOM);
        STRING2FUNC.put("pdfdomhtml", FuncCode.PDFDOMHTML);
        STRING2FUNC.put("pdfhtml", FuncCode.PDFHTML);
        STRING2FUNC.put("pdftext", FuncCode.PDFTEXT);
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
                case MPSTREE:
                    mpstree(args);
                    break;
                case OCR:
                    ocr(args);
                    break;
                case PDFDOM:
                    pdfdom(args);
                    break;
                case PDFDOMHTML:
                    pdfdomhtml(args);
                    break;
                case PDFHTML:
                    pdfhtml(args);
                    break;
                case PDFTEXT:
                    pdftext(args);
                    break;
                default:
                    System.out.println("Function \"" + funcName + "\" not implemented.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mpstree(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = cmdParser.parse(options, params);
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
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);

            File outputFile = new File(pdfFile.getParent(), baseName + "_mpstree.html");

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            MPSTree parser = new MPSTree();
            parser.setDisableImageData(true);
            Document domDoc = parser.createDOM(pdfDoc);

            Writer output = new PrintWriter(outputFile, "utf-8");
            parser.writeText(pdfDoc, output);
            output.close();

            /*
            Element topElem = domDoc.getDocumentElement();
            System.out.printf("topElem=" + topElem.getTagName() + "\n");

            NodeList imgList = domDoc.getElementsByTagName("img");
            for (int i = 0; i < imgList.getLength(); i++) {
                Element elem = (Element)imgList.item(i);
                String src = elem.getAttribute("src");
                System.out.printf("Image #%03d: %s\n", (i+1), src);
            }
            */
            pdfDoc.close();
        }
    }

    private static void pdfdom(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = cmdParser.parse(options, params);
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
                throw new Exception(String.format("Error: invalid file path \"%s\".", pdfFileName));
            }

            int extPos = pdfFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    pdfFile.getName() : pdfFile.getName().substring(0, extPos);

            File outputFile = new File(pdfFile.getParent(), baseName + "_pdfdom.html");

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDFDomTree parser = new PDFDomTree();
            parser.setDisableImageData(true);
            Document domDoc = parser.createDOM(pdfDoc);

            Element topElem = domDoc.getDocumentElement();
            System.out.printf("topElem=" + topElem.getTagName() + "\n");

            NodeList imgList = domDoc.getElementsByTagName("img");
            for (int i = 0; i < imgList.getLength(); i++) {
                Element elem = (Element)imgList.item(i);
                String src = elem.getAttribute("src");
                System.out.printf("Image #%03d: %s\n", (i+1), src);
            }

            XMLDocumentWriter.write(domDoc, outputFile.getAbsolutePath(), true);
            pdfDoc.close();
        }
    }

    private static void pdfdomhtml(
            String[] params
        ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

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
            formatter.printHelp(String.format("%s [options] pdf_file [pdf_file...]", params[0]), options);
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

            File outputFile = new File(pdfFile.getParent(), baseName + "_pdfdom.html");

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            Writer output = new PrintWriter(outputFile, "utf-8");
            new PDFDomTree().writeText(pdfDoc, output);
            output.close();
            pdfDoc.close();
        }
    }

    private static void pdfhtml(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

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
            String msg = String.format("%s [options] pdf_file [pdf_file...]", params[0]);
            formatter.printHelp(msg, options);
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

            File outputFile = new File(pdfFile.getParent(), baseName + "_pdftext2html.html");

            PDDocument pdfDoc = PDDocument.load(pdfFile);
            Writer writer = new PrintWriter(outputFile, "utf-8");
            new PDFText2HTML().writeText(pdfDoc, writer);
            writer.close();
            pdfDoc.close();
        }
    }

    private static void pdftext(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = cmdParser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            String msg = String.format("%s [options] pdf_file [pdf_file...]", params[0]);
            formatter.printHelp(msg, options);
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

            File outputFile = new File(pdfFile.getParent(), baseName + "_pdftext.html");

            //PDDocument pdfDoc = PDDocument.load(pdfFile);
            PDFParser parser = new PDFParser(new RandomAccessFile(pdfFile, "r"));
            parser.parse();

            COSDocument cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            String parsedText = pdfStripper.getText(pdDoc);

            PrintWriter writer = new PrintWriter(outputFile, "utf-8");
            writer.print(parsedText);
            writer.close();
        }
    }

    private static void ocr(
            String[] params
    ) throws Exception
    {
        System.out.printf("Executing function \"%s\"\n", params[0]);

        Options options = new Options();

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmdLine = null;
        boolean displayHelp = params.length < 2;
        if (params.length > 1) {
            try {
                cmdLine = cmdParser.parse(options, params);
            } catch (ParseException e) {
                displayHelp = true;
                System.out.printf("Error: %s\n", e.getLocalizedMessage());
            }
        }
        if (displayHelp) {
            HelpFormatter formatter = new HelpFormatter();
            String msg = String.format("%s [options] image_file [image_file...]", params[0]);
            formatter.printHelp(msg, options);
            return;
        }

        /*
        int extPos = imgFile.getName().lastIndexOf(".");
        String baseName = extPos == -1 ?
                imgFile.getName() : imgFile.getName().substring(0, extPos);
        File outputFile = new File(imgFile.getParent(), baseName + "_ocr.html");
        */

        List<String> imgFileList = cmdLine.getArgList();
        String[] imgInputList = new String[imgFileList.size()-1];
        String[] imgOutputList = new String[imgFileList.size()-1];
        List<ITesseract.RenderedFormat> formatList = new ArrayList<>();
        int ndx = 0;
        for (String imgFileName : imgFileList.subList(1, imgFileList.size())) {
            File imgFile = new File(imgFileName);
            int extPos = imgFile.getName().lastIndexOf(".");
            String baseName = extPos == -1 ?
                    imgFile.getName() : imgFile.getName().substring(0, extPos);
            imgInputList[ndx] = imgFileName;
            imgOutputList[ndx] = baseName + "_ocr.xml";
            ndx += 1;
            formatList.add(ITesseract.RenderedFormat.ALTO);
        }
        ITesseract tess = new Tesseract();
        tess.setTessVariable(" tessedit_create_alto", "1");

        File outputFile = new File("test_ocr.txt");
        PrintWriter writer = new PrintWriter(outputFile, "utf-8");

        for (String imgFileName : imgFileList.subList(1, imgFileList.size())) {
            File imgFile = new File(imgFileName);
            String parsedText = tess.doOCR(imgFile);
            writer.write(parsedText);
        }
        /*
        List<OCRResult> resultList = tess.createDocumentsWithResults(imgInputList, imgOutputList, formatList, TessAPI.TessPageIteratorLevel.RIL_PARA);
        for (OCRResult result : resultList) {
            //System.out.println(result.toString());
        }
        */
        writer.close();
    }
}
