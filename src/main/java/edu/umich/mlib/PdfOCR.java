package edu.umich.mlib;

import org.apache.commons.cli.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.fit.pdfdom.PDFDomTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;
import java.util.List;

public class PdfOCR
{
    private enum FuncCode {
        MPSTREE,
        PDFDOM,
        PDFDOMHTML,
        PDFTEXT
    }

    private static Map<String, FuncCode> STRING2FUNC = new HashMap<>();
    static {
        STRING2FUNC.put("mpstree", FuncCode.MPSTREE);
        STRING2FUNC.put("pdfdom", FuncCode.PDFDOM);
        STRING2FUNC.put("pdfdomhtml", FuncCode.PDFDOMHTML);
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
                case PDFDOM:
                    pdfdom(args);
                    break;
                case PDFDOMHTML:
                    pdfdomhtml(args);
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

            File outputFile = new File(pdfFile.getParent(), baseName + "_pdfdom.html");

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

    private static void pdftext(
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
}
