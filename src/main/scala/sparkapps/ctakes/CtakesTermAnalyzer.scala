package sparkapps.ctakes

import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory.{RemoveEnclosedLookupWindows, CopyNPChunksToLookupWindowAnnotations}
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator
import org.apache.ctakes.core.ae.{TokenizerAnnotatorPTB, SimpleSegmentAnnotator}
import org.apache.ctakes.typesystem.`type`.syntax.BaseToken
import org.apache.uima.analysis_engine.AnalysisEngine
import org.apache.uima.jcas.JCas
import org.uimafit.factory.{JCasFactory, AnalysisEngineFactory, AggregateBuilder}
import org.uimafit.pipeline.SimplePipeline
import org.uimafit.util.JCasUtil

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
  def getDefaultPipeline():AnalysisEngine  = {
    var builder = new AggregateBuilder
    builder.add(SimpleSegmentAnnotator.createAnnotatorDescription());
    builder.add(org.apache.ctakes.core.ae.SentenceDetector.createAnnotatorDescription());
    builder.add(TokenizerAnnotatorPTB.createAnnotatorDescription());
    builder.add(ContextDependentTokenizerAnnotator.createAnnotatorDescription());
    builder.add(org.apache.ctakes.postagger.POSTagger.createAnnotatorDescription());
    builder.add(org.apache.ctakes.chunker.ae.Chunker.createAnnotatorDescription());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[CopyNPChunksToLookupWindowAnnotations]));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[RemoveEnclosedLookupWindows]));
    //builder.add(UmlsDictionaryLookupAnnotator.createAnnotatorDescription()); builder.add(PolarityCleartkAnalysisEngine.createAnnotatorDescription()); return builder.createAggregateDescription(); }
    builder.createAggregate()
  }


  def analyze(text:String):Any = {
    val aed:AnalysisEngine= getDefaultPipeline();
    val jcas:JCas = JCasFactory.createJCas();
    jcas.setDocumentText(text);
    SimplePipeline.runPipeline(jcas, aed);
    val iter = JCasUtil.select(jcas,classOf[BaseToken]).iterator()
    while(iter.hasNext)
    {
      val entity = iter.next();
      //for demonstration purposes , we print all this stuff.
      System.out.print("---"+entity.getCoveredText + " " + entity.getPartOfSpeech+"---");
    }
    //return the iterator.
    JCasUtil.select(jcas,classOf[BaseToken]).iterator()
  }

  def main(args: Array[String]): Unit = {
    System.out.println(analyze("The patient might have diabetes.  His blood sugare and glucose levels are off the charts.  Symptoms include naseau and light headedness."))
  }
}
