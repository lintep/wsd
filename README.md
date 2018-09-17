# Unsupervised Word Sense Disambiguation
 **Java(8)** project new unsupervised co-occurrence based word sense disambiguation just use a large scale monolingual corpus as a knowledge resource. This project is powered by **_Spark_** and _**MapReduce**_ distributed framework and it is easily extend for other languages.

Download **wikipedia** dump file and do clean from ([here](https://github.com/lintep/wikidump)) as a large scale  monolingual corpus.

### Step1: Download and install
1. `git clone https://github.com/lintep/wsd.git`
2. `cd wsd`
3. `mvn install`
4. `cd target`

### Step2: Extract POS tagged sentences
`java -cp lintep-wsd-1.0.jar:lib/* nlp.spark.SparkExtractPosTaggedSentence args[0] {args[1]} {args[2]}`

     args[0]: Paragraph input path or file address

     args[1]: PTokenizer model path (used for Persian), project resource include that.

     args[2]: POS tagged token fileaddress (used for persian)

### Step4: Extract tokens co-occurrence
`java -cp lintep-wsd-1.0.jar:lib/* nlp.spark.SparkExtractCoocurrance args[0] args[1] args[2]`

     args[0]: POS tagged sentences path (Step2 result path). 
     args[1]: Legal POS tags splits by ',' used to filter sentence tokens by POS.
          for example N,AJ used for noun and adjective.
     args[2]: Co-occurrence graph construction window size list, split by ',' and 
              used to extract filter outside co-occurrence pair tokens.
          for example 2,3 lead to extract co-occurrence in two directory for window size 2 and 3.


### Step4: Store extracted co-occurrence into mysql database. 
`java -cp lintep-wsd-1.0.jar:lib/* nlp.languagemodel.MapReduceToMysql args[0]`

     args[0]: wsd.properties file address.

### Step5: Disambiguate your examples.
`java -cp com.lintep-1.0.jar:lib/* nlp.wsd.WSDSystem args[0]`

     args[0]: wsd.properties file address.
