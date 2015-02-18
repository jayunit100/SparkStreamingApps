package sparkapps.ctakes

import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory.{RemoveEnclosedLookupWindows, CopyNPChunksToLookupWindowAnnotations}
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator
import org.apache.ctakes.core.ae.{TokenizerAnnotatorPTB, SimpleSegmentAnnotator}
import org.apache.ctakes.typesystem.`type`.syntax.BaseToken
import org.apache.uima.analysis_engine.AnalysisEngine
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.jcas.JCas
import org.apache.uima.fit.component.JCasAnnotator_ImplBase
import org.apache.uima.fit.factory.AggregateBuilder
import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.fit.factory.ExternalResourceFactory
import org.apache.uima.fit.factory.JCasFactory
import org.apache.uima.fit.pipeline.SimplePipeline
import org.apache.uima.fit.util.JCasUtil

import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine
import org.apache.ctakes.assertion.medfacts.cleartk.ConditionalCleartkAnalysisEngine
import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine
import org.apache.ctakes.assertion.medfacts.cleartk.HistoryCleartkAnalysisEngine
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine
import org.apache.ctakes.dictionary.lookup.ae.DictionaryLookupAnnotator
import org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator
import org.apache.ctakes.drugner.ae.DrugMentionAnnotator
import org.apache.ctakes.core.resource.JdbcConnectionResourceImpl
import org.apache.ctakes.typesystem.`type`.textsem.IdentifiedAnnotation
import org.apache.ctakes.typesystem.`type`.refsem.UmlsConcept;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory
import org.apache.ctakes.core.resource.FileLocator
import org.apache.ctakes.core.resource.FileResourceImpl
import org.apache.ctakes.dictionary.lookup2.ae.AbstractJCasTermAnnotator
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator

/**
 *
 * This is a module which analyzes terms
 * and returns their metadata.  It is a wrapper
 * to the CTakes library, used for spark
 * streaming CTakes analysis of Tweets.
 */
object CtakesTermAnalyzer {

  /**
   * Simple pipeline.
   */
  def getDefaultPipeline():AnalysisEngineDescription  = {
    var builder = new AggregateBuilder
    builder.add(SimpleSegmentAnnotator.createAnnotatorDescription());
    builder.add(org.apache.ctakes.core.ae.SentenceDetector.createAnnotatorDescription());
    builder.add(TokenizerAnnotatorPTB.createAnnotatorDescription());
    builder.add(ContextDependentTokenizerAnnotator.createAnnotatorDescription());
    builder.add(org.apache.ctakes.postagger.POSTagger.createAnnotatorDescription());
    builder.add(org.apache.ctakes.chunker.ae.Chunker.createAnnotatorDescription());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[CopyNPChunksToLookupWindowAnnotations]));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[RemoveEnclosedLookupWindows]));
    builder.add(getDictionaryDescription());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[DrugMentionAnnotator]));
    builder.add(ClearNLPDependencyParserAE.createAnnotatorDescription());
    builder.add(PolarityCleartkAnalysisEngine.createAnnotatorDescription()); 
    builder.add(SubjectCleartkAnalysisEngine.createAnnotatorDescription());
    builder.add(HistoryCleartkAnalysisEngine.createAnnotatorDescription());
    builder.add(GenericCleartkAnalysisEngine.createAnnotatorDescription());
    builder.add(UncertaintyCleartkAnalysisEngine.createAnnotatorDescription());
    builder.add(ConditionalCleartkAnalysisEngine.createAnnotatorDescription());
    builder.createAggregateDescription();
  }


 /**
 * Configuration for the cTAKES Dictionary
 */
def getDictionaryDescription():AnalysisEngineDescription = {
AnalysisEngineFactory.createEngineDescription(classOf[DictionaryLookupAnnotator],
          "LookupDescriptor",
          ExternalResourceFactory.createExternalResourceDescription(
              classOf[FileResourceImpl],
              FileLocator.locateFile("org/apache/ctakes/dictionary/lookup/LookupDesc_Db.xml")),
          "DbConnection",
          ExternalResourceFactory.createExternalResourceDescription(
              classOf[JdbcConnectionResourceImpl],
              "",
              JdbcConnectionResourceImpl.PARAM_DRIVER_CLASS,
              "org.hsqldb.jdbcDriver",
              JdbcConnectionResourceImpl.PARAM_URL,
              // Should be the following but it's WAY too slow
              // "jdbc:hsqldb:res:/org/apache/ctakes/dictionary/lookup/umls2011ab/umls"),
              "jdbc:hsqldb:file:org/apache/ctakes/dictionary/lookup/umls2011ab/umls"),
          "RxnormIndexReader",
          ExternalResourceFactory.createExternalResourceDescription(
              classOf[JdbcConnectionResourceImpl],
              "",
              JdbcConnectionResourceImpl.PARAM_DRIVER_CLASS,
              "org.hsqldb.jdbcDriver",
              JdbcConnectionResourceImpl.PARAM_URL,
              "jdbc:hsqldb:file:org/apache/ctakes/dictionary/lookup/rxnorm-hsqldb/umls"),
          "OrangeBookIndexReader",
          ExternalResourceFactory.createExternalResourceDescription(
              classOf[JdbcConnectionResourceImpl],
              "",
              JdbcConnectionResourceImpl.PARAM_DRIVER_CLASS,
              "org.hsqldb.jdbcDriver",
              JdbcConnectionResourceImpl.PARAM_URL,
              "jdbc:hsqldb:file:org/apache/ctakes/dictionary/lookup/orange_book_hsqldb/umls")
              );
}


def analyze(text:String):Any = {
    val aed:AnalysisEngineDescription= getDefaultPipeline();
    val jcas:JCas = JCasFactory.createJCas();
    jcas.setDocumentText(text);
    SimplePipeline.runPipeline(jcas, aed);
    val iter = JCasUtil.select(jcas,classOf[IdentifiedAnnotation]).iterator()
    while(iter.hasNext)
    {
      val entity = iter.next();
      //for demonstration purposes , we print all this stuff.
	val mentions = entity.getOntologyConceptArr;
	var i = 0;
	if (mentions!=null && mentions.size > 0) {
 		val uniqueCuis = scala.collection.mutable.Set[String]()
    	 for (i <- i to mentions.size -1) {
	  if(mentions.get(i)!=null && mentions.get(i).isInstanceOf[UmlsConcept]){
	    val concept = mentions.get(i).asInstanceOf[UmlsConcept] ;
	     uniqueCuis += concept.getCui;
	  }
	 }
	 uniqueCuis.foreach(println);
        }
	System.out.print("---"+entity.getCoveredText + " " + entity.getPolarity+"---");
	System.out.print(entity);
    }
    //return the iterator.
    JCasUtil.select(jcas,classOf[BaseToken]).iterator()
	jcas.reset();
  }

  def main(args: Array[String]): Unit = {
    System.out.println(analyze("The patient did not have diabetes.  He took 50mg of aspirin twice daily for his pain. Medications include Synthroid, Crestor, Nexium, Ventolin HFA, Advair Diskus, Diovan, Lantus Solostar, Cymbalta, Vyvanse, Lyrica.  His blood sugare and glucose levels are off the charts.  Symptoms include naseau and light headedness."))
  }
}
