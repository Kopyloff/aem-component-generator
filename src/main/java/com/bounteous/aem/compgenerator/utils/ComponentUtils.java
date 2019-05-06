/*
 * ***********************************************************************
 * BOUNTEOUS CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Bounteous
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of Bounteous and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Bounteous
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Bounteous.
 * ***********************************************************************
 */

package com.bounteous.aem.compgenerator.utils;

import com.bounteous.aem.compgenerator.Constants;
import com.bounteous.aem.compgenerator.exceptions.GeneratorException;
import com.bounteous.aem.compgenerator.models.GenerationConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.Map;

/**
 * <p>
 * ComponentUtils class helps in building elements of component
 * like folders, xml file, html with content based data-config file.
 */

public class ComponentUtils {

    private GenerationConfig generationConfig;
    private Map<String, String> templateValueMap;

    public ComponentUtils(GenerationConfig config) {
        this.generationConfig = config;
        this.templateValueMap = CommonUtils.getTemplateValueMap(generationConfig);
    }

    /**
     * builds your base folder structure of a component includes component folder
     * itself, _cq_dialog with field properties, dialogglobal with properties-global,
     * HTML, clientlibs folder.
     */
    public void _buildComponent() throws Exception {
        if (generationConfig == null) {
            throw new GeneratorException("Config file cannot be empty / null !!");
        }

        //creates base component folder.
        createFolderWithContentXML(generationConfig.getCompDir(), Constants.TYPE_COMPONENT);

        //create _cq_dialog xml with user input properties in json.
        DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_DIALOG);

        //create dialogglobal xml file with user input global properties in json.
        if (generationConfig.getOptions().getGlobalProperties() != null &&
                generationConfig.getOptions().getGlobalProperties().size() > 0) {
            DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_GLOBAL);
        }

        //create dialogshared xml file with user input global properties in json.
        if (generationConfig.getOptions().getSharedProperties() != null &&
                generationConfig.getOptions().getSharedProperties().size() > 0) {
            DialogUtils.createDialogXml(generationConfig, Constants.DIALOG_TYPE_SHARED);
        }

        //builds clientLib and placeholder files for js and css.
        createClientLibs();

        //builds sightly html file using htl template from resource.
        createHtl();

        System.out.println("--------------* Component '" + generationConfig.getName() + "' successfully generated *--------------");

    }

    /**
     * builds default clientlib structure with js and css file under folder.
     */
    private void createClientLibs() {
        String clientLibDirPath = generationConfig.getCompDir() + "/clientlibs";
        try {
            if (generationConfig.getOptions().isHasJs() || generationConfig.getOptions().isHasCss()) {
                createFolderWithContentXML(clientLibDirPath, Constants.TYPE_SLING_FOLDER);
                if (generationConfig.getOptions().isHasCss()) {
                    CommonUtils.createFolder(clientLibDirPath + "/site/css");
                    CommonUtils.createFileWithCopyRight(clientLibDirPath + "/site/css/" + generationConfig.getName() + ".less", templateValueMap);
                }
                if (generationConfig.getOptions().isHasJs()) {
                    CommonUtils.createFolder(clientLibDirPath + "/site/js");
                    CommonUtils.createFileWithCopyRight(clientLibDirPath + "/site/js/" + generationConfig.getName() + ".js", templateValueMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneratorException("Exception while creating clientLibs : " + clientLibDirPath);
        }
    }

    /**
     * creates a folder on given path and adds content.xml file based on the folderType.
     *
     * @param path
     * @param folderType
     * @throws Exception
     */
    private void createFolderWithContentXML(String path, String folderType)
            throws Exception {
        Path folderPath = CommonUtils.createFolder(path);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = XMLUtils.createRootElement(doc);

            //set attributes based on folderType.
            if (folderType.equalsIgnoreCase(Constants.TYPE_COMPONENT)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
                rootElement.setAttribute(Constants.PROPERTY_JCR_TITLE, generationConfig.getTitle());
                rootElement.setAttribute("componentGroup", generationConfig.getGroup());
            } else if (folderType.equalsIgnoreCase(Constants.TYPE_SLING_FOLDER)) {
                rootElement.setAttribute(Constants.JCR_PRIMARY_TYPE, folderType);
            }
            doc.appendChild(rootElement);
            XMLUtils.transformDomToFile(doc, folderPath + "/" + Constants.FILENAME_CONTENT_XML);
            System.out.println("Created : " + folderPath + "/" + Constants.FILENAME_CONTENT_XML);
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating Folder/xml : " + path);
        }
    }



    /**
     * create default HTML file based the provided template.
     */
    private void createHtl() {
        try {
            CommonUtils.createFileWithCopyRight(generationConfig.getCompDir() +
                    "/" + generationConfig.getName() + ".html",
                    templateValueMap);

            System.out.println("Created : " + generationConfig.getCompDir() +
                    "/" + generationConfig.getName() + ".html");
        } catch (Exception e) {
            throw new GeneratorException("Exception while creating HTML : " + generationConfig.getCompDir());
        }
    }






}

