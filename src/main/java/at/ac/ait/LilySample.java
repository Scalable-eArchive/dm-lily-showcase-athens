package at.ac.ait;

import org.lilyproject.client.LilyClient;
import org.lilyproject.repository.api.*;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LilySample {
	
	private LRepository repository;
	private LTable table;
	
    public static void main(String[] args) throws Exception {
        new LilySample().run();
    }

    public void run() throws Exception {
        LilyClient lilyClient = new LilyClient(System.getProperty("zkConn", "localhost:2181"), 20000);
        repository = lilyClient.getDefaultRepository();
        table = repository.getDefaultTable();

        /*
        System.out.println("Importing schema");
        InputStream is = LilySample.class.getResourceAsStream("schema.json");
        JsonImport.loadSchema(repository, is);
        is.close();
        System.out.println("Schema successfully imported");
        */
        
        createRecordFromXML("book1.xml");
        createRecordFromXML("book2.xml");
        
        lilyClient.close();
    }

    private static QName q(String name) {
        return new QName("at.ac.ait", name);
    }
    
    private void createRecordFromXML(String filename) throws Exception {
        //
        // Create a record
        //
        System.out.println("Creating a record");
        Record record = table.newRecord();
        record.setId(repository.getIdGenerator().newRecordId());
        record.setRecordType(q("Book"));
        
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder(); 
    	
    	Document doc = db.parse(new File(filename));
    	
    	NodeList nodeList = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
	            String field = n.getNodeName();
	            String type = repository.getTypeManager().getFieldTypeByName(q(field)).getValueType().getName();
	            String stringValue = n.getChildNodes().item(0).getNodeValue();
	            Object value = stringValue;
	            if (type.equals("DATE")) {
	            	value = ISODateTimeFormat.localDateParser().parseDateTime(stringValue).toLocalDate();
	            } else if (type.equals("DOUBLE")) {
	            	value = Double.parseDouble(stringValue);
	            }
	            record.setField(q(field), value);
            }
        }
        
        // We use the createOrUpdate method as that one can automatically recover
        // from connection errors (idempotent behavior, like PUT in HTTP).
        table.createOrUpdate(record);
        System.out.println("Record created: " + record.getId());
    }
    
}
