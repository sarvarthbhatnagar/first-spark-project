package com.mycompany.app;

import java.util.Arrays;

// Basic Spark stuff
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

// For the "Word count" example
import org.apache.spark.api.java.JavaPairRDD;
import scala.Tuple2;

import static com.mycompany.app.SerializableComparator.serialize;

/**
* Hello world!
*
*/
public class App 
{
	public static void main( String[] args )
	{
		new App().Run();
	}
	
	public void Run()
	{
		// --------------------------------------------------------
		// Initialization
		System.out.println( "Hello World!" );

		SparkConf conf = new SparkConf().setAppName("firstSparkProject").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		sc.setLogLevel("WARN"); // Don't want the INFO stuff
		
		String path = "SPARKREADME.md";

		System.out.println("Trying to open: " + path);
		JavaRDD<String> jrdd = sc.textFile(path.toString());
		
		// --------------------------------------------------------
		// First information
		System.out.println("#lines: " + jrdd.count() );
		System.out.println("#lines having > 5 words: " + jrdd.filter(line -> line.split(" ").length > 5 ).count());
		System.out.println("#lines containing Spark: " + jrdd.filter(line -> line.contains("Spark")).count());
		
		// Number of characters in the file
		JavaRDD<Integer> jrddLength = jrdd.map(line -> line.length());
		System.out.println("#characters: " + jrddLength.reduce((x,y) -> x + y));
		
		// --------------------------------------------------------
		// Word count
		JavaRDD<String> words = jrdd.flatMap(s -> Arrays.asList(s.split(" ")).iterator());
		JavaPairRDD<String, Integer> pairs = words.mapToPair(word -> new Tuple2<String, Integer>(word, word.isEmpty() ? 0 : 1));
		JavaPairRDD<String, Integer> wordCount = pairs.reduceByKey((x,y) -> x + y);
		
		System.out.println("#words: " + words.count());
		System.out.println("#unique words: " + wordCount.count());
		
		System.out.println("Average: " + wordCount.mapToDouble(tuple -> (double)tuple._2).mean());
		
		Tuple2<String, Integer> wordMax = wordCount.max( serialize((t1, t2) -> t1._2 - t2._2 ) );
		System.out.println("Max: \"" + wordMax._1 + "\" appears " + wordMax._2 + " times");
		
		// Displaying the counting
		//wordCount.foreach(tuple -> System.out.println("\"" + tuple._1 + "\" appears " + tuple._2 + " times"));

		// --------------------------------------------------------
		// De-initialization
		sc.stop();
		sc.close();
		
		System.out.println("END");
	}
}
