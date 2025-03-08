package com.example.messageRouting.service;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.example.messageRouting.adapter.cache.CountryCrossRefCache;
import com.example.messageRouting.entity.CountryCrossRef;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CrossRefService {
	
	@Autowired
	CountryCrossRefCache countryCrossRefCache;
	
	
	public void process(Exchange exchange) {
		try {
			String keyXPath= exchange.getIn().getHeader("keyXPath",String.class);
			String replaceXPath= exchange.getIn().getHeader("replaceXPath",String.class);
			String refCollection = exchange.getIn().getHeader("refCollection",String.class);
			//replace replaceXPath value with value of keyXPath

			String keyField = keyXPath.substring(keyXPath.lastIndexOf('/')+1);
			String replaceField = replaceXPath.substring(replaceXPath.lastIndexOf('/')+1);
			String keyValue = exchange.getContext().resolveLanguage("xpath")
			        .createExpression(keyXPath+"/text()")
			        .evaluate(exchange, String.class);
			String replaceValue="";
			System.out.println("+++++keyValue:"+keyValue+", keyXPath:"+keyXPath+" replaceField:"+replaceField);
			if("countryCrossRef".equals(refCollection)) {
				System.out.println("+++++refCollection:"+refCollection);
				CountryCrossRef countryCrossRef =  countryCrossRefCache.getCountryCrossRefByKey(keyValue);
				System.out.println("+++++countryCrossRef:"+countryCrossRef);
				replaceValue = countryCrossRef.getValue();
			}
			String xmlBody = exchange.getIn().getBody(String.class);
			Document document = null;
			try {
				// Parse XML into DOM
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(new InputSource(new StringReader(xmlBody)));
			} catch (Exception e) {
				log.error("Exception occured while parsing XML to DOM: ",e);
			} 

			try {
				// Locate <keyField> element and update its value
				NodeList replaceFieldNodes = document.getElementsByTagName(replaceField);
				if (replaceFieldNodes.getLength() > 0) {
					replaceFieldNodes.item(0).setTextContent(replaceValue);
				}
			} catch (Exception e) {
				log.error("Exception occured while replacing DOM nodes: ",e);
			}
			String updatedXmlBody = null;
			try {
				// Convert DOM back to XML String
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(document), new StreamResult(writer));
				updatedXmlBody = writer.toString();
			} catch (Exception e) {
				log.error("Exception occured while converting DOM to XML: ",e);
			} 

			// Set updated XML back to Exchange
			exchange.getIn().setBody(updatedXmlBody);
		} catch (Exception e) {
			log.error("Exception occured in CrossRefService ",e);
		} 	
		
	}
}
