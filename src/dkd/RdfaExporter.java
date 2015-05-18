package dkd;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentExporter;
import gate.FeatureMap;
import gate.GateConstants;
import gate.Factory;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
@CreoleResource(name = "RDFa Lite Exporter", comment = "Exports Annotations as RDFa Lite", tool = true, autoinstances = @AutoInstance, icon="")
public class RdfaExporter extends DocumentExporter
{
    public RdfaExporter()
    {
        super("HTML5 RDFa","html","text/html");
    }

    @Override
    public void export(Document document, OutputStream out, FeatureMap options) throws IOException
    {
        PrintStream pout = new PrintStream(out);
        AnnotationSet outputAS = document.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
        System.out.println("reached0");
        
        //only work in body
        AnnotationSet body = outputAS.get("html");
        if (body.size() != 1)
        {
            System.out.println("no html");
            return;
        }
        body = outputAS.get("body");
        if (body.size() != 1)
        {
            System.out.println("no body");
            return;
        }
        AnnotationSet mentions = document.getAnnotations().get("Mention",body.firstNode().getOffset(),body.lastNode().getOffset());
        
		//store created annotations so we can remove them later, not relevant in wasp
		//but quite relevant in gate.app usecase
        Set<Integer> created = new HashSet<Integer>();

        try
        {
            for (Annotation annotation : mentions)
            {   
                FeatureMap params = Factory.newFeatureMap();
                params.put("vocab", "http://dbpedia.org/ontology/");
                params.put("resource", annotation.getFeatures().get("inst").toString());

                String type =  annotation.getFeatures().get("airpSpecificClass").toString();
                if (type.contains("|"))
                    type = type.split("\\|")[0];

                if (type.contains(":"))
                    type = type.split(":")[1];

                params.put("typeof",type);
                
                Long start = annotation.getStartNode().getOffset();
                Long end = annotation.getEndNode().getOffset();
              
                created.add(outputAS.add(start, end, "span", params));
            }
            pout.println(document.toXml(null, false));
        }
        catch(Exception e)
        {
            throw new IOException(e);
        }
        finally
        {
            for (Integer id : created)
                outputAS.remove(outputAS.get(id));
        }    
    }
}
