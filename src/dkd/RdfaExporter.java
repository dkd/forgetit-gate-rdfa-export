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

    private String extractClass(String input)
    {
        if (input.contains("|"))
            input = input.split("\\|")[0];

        if (input.contains(":"))
            input = input.split(":")[1];

        return input;
    }

    @Override
    public void export(Document document, OutputStream out, FeatureMap options) throws IOException
    {
        PrintStream pout = new PrintStream(out);
        AnnotationSet outputAS = document.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
        AnnotationSet mentions = document.getAnnotations().get("Mention");

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

                params.put("typeof",extractClass(annotation.getFeatures().get("dbpSpecificClass").toString()));
                params.put("base",extractClass(annotation.getFeatures().get("dbpInterestingClass").toString()));

                Long start = annotation.getStartNode().getOffset();
                Long end = annotation.getEndNode().getOffset();

                created.add(outputAS.add(start, end, "span", params));
            }

            String result = document.toXml(null, false);
            //the gate html document implementation will have added this stuff to the ORIGINAL_MARKUPS
            //we do not want these in the RTE or other endpoints
            if (options.containsKey("stripOuter") && ((String)options.get("stripOuter")).equals("true"))
                {
                    result = result.replace("<html><head></head><body>","");
                    result = result.replace("</body></html>","");
                }
            pout.println(result);
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
