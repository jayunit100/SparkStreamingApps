package sparkapps

import java.text.BreakIterator

import opennlp.tools.postag.POSTagger
import opennlp.tools.sentdetect.{SentenceDetectorME, SentenceModel, SentenceDetector}
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory.{RemoveEnclosedLookupWindows, CopyNPChunksToLookupWindowAnnotations}
import org.apache.ctakes.constituency.parser.ae.ConstituencyParser
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator
import org.apache.ctakes.core.ae.{TokenizerAnnotatorPTB, SimpleSegmentAnnotator}
import org.apache.ctakes.dependency.parser.ae.{ClearNLPSemanticRoleLabelerAE, ClearNLPDependencyParserAE}
import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE._
import org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator
import org.apache.ctakes.typesystem.`type`.syntax.BaseToken
import org.apache.ctakes.typesystem.`type`.textsem.IdentifiedAnnotation
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.jcas.JCas
import org.cleartk.chunker.Chunker
import org.uimafit.factory.{AnalysisEngineFactory, AggregateBuilder, JCasFactory}
import org.uimafit.pipeline.SimplePipeline
import org.uimafit.util.JCasUtil

import scala.collection.JavaConverters._

/**
 * Created by jay on 10/10/14.
 */
object CTakesExample {

  def getDefaultPipeline():AnalysisEngineDescription = {
    /**
     * not sure if useful.
    val modelIn = getClass().getResourceAsStream("en-sent.bin");
    val sentenceModel = new SentenceModel(modelIn);
    modelIn.close();
    val sentenceDetector = new SentenceDetectorME(sentenceModel);
     */

    def builder =
      new AggregateBuilder
    builder.add(SimpleSegmentAnnotator.createAnnotatorDescription());
    builder.add(org.apache.ctakes.core.ae.SentenceDetector.createAnnotatorDescription());

    builder.add(TokenizerAnnotatorPTB.createAnnotatorDescription());
    builder.add(ContextDependentTokenizerAnnotator.createAnnotatorDescription());

    builder.add(org.apache.ctakes.postagger.POSTagger.createAnnotatorDescription());
    builder.add(org.apache.ctakes.chunker.ae.Chunker.createAnnotatorDescription());

    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[CopyNPChunksToLookupWindowAnnotations]));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(classOf[RemoveEnclosedLookupWindows]));
    //builder.add(UmlsDictionaryLookupAnnotator.createAnnotatorDescription());
    builder.add(PolarityCleartkAnalysisEngine.createAnnotatorDescription());
    return builder.createAggregateDescription();

  }
  def main(args: Array[String]) {

    val aed:AnalysisEngineDescription = getDefaultPipeline();
      val jcas:JCas = JCasFactory.createJCas();
      jcas.setDocumentText("The patient is suffering from extreme pain due to shark bite. Recommend continuing use of aspirin, oxycodone, and coumadin. atient denies smoking and chest pain. Patient has no cancer. There is no sign of multiple sclerosis. Continue exercise for obesity and hypertension. ");

      SimplePipeline.runPipeline(jcas, aed);

      //Print out the tokens and Parts of Speech

      val iter = JCasUtil.select(jcas,classOf[org.apache.ctakes.typesystem.`type`.textspan.Sentence]).iterator()
      //val iter = JCasUtil.select(jcas, classOf[BaseToken]).iterator()
      //val iter = JCasUtil.selectAll(jcas).iterator();
      System.out.println(iter.hasNext);


      while(iter.hasNext){
        val entity = iter.next();
        //System.out.println(entity.toString())
        //System.out.println(entity.getCAS)
        System.out.println(entity)

        //System.out.println( "Text:" + entity.getCoveredText()) //+ " === POS: " + entity.getSubject + " " + entity.getConfidence + " " + entity.getSofa );
      }

  }

}
