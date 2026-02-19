# Visual Execution Flow & Diagrams

## COMPLETE TEST EXECUTION FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MAVEN TEST EXECUTION                          │
│  Command: mvn clean test                                             │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      1. CONFIGURATION LOAD                           │
│  ┌─ testng.xml (test configuration)                                 │
│  ├─ config.properties (loaded by ConfigManager)                     │
│  └─ .env file (loaded by ConfigManager at startup)                 │
│                                                                      │
│  Result: All configuration in memory, ready to use                 │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              2. TEST CLASS INSTANTIATION                             │
│  TestNG instantiates:                                               │
│  ├─ ArticleScrapingTest instance                                   │
│  └─ TranslationAnalysisTest instance                               │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              3. @BeforeMethod: setUp() EXECUTED                      │
│                                                                      │
│  Parameters from testng.xml:                                        │
│  env = "local"                                                      │
│  browserName = "chrome"                                             │
│  browserVersion = "latest"                                          │
│  os = "Windows"                                                     │
│  osVersion = "11"                                                   │
│  resolution = "1920x1080"                                           │
│                                                                      │
│  DriverManager.initializeDriver(env, browserName, ...)              │
│       │                                                              │
│       ├─ IF env = "local":                                          │
│       │   └─ WebDriverFactory.createLocalDriver("chrome")          │
│       │       ├─ WebDriverManager.chromedriver().setup()           │
│       │       ├─ Create ChromeDriver with options                  │
│       │       └─ Return ChromeDriver                               │
│       │                                                              │
│       ├─ Set implicit wait: 15 seconds                              │
│       ├─ Set page load timeout: 60 seconds                          │
│       ├─ Maximize window                                            │
│       └─ threadLocalDriver.set(driver)                              │
│                                                                      │
│  Result: WebDriver instance created and stored in ThreadLocal     │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              4. @Test: TEST METHOD EXECUTES                          │
│              (ArticleScrapingTest.testArticleScraping)              │
│                                                                      │
│  Code Flow:                                                          │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ String appUrl = ConfigManager.get("app.url")         │           │
│  │ // appUrl = "https://elpais.com"                    │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     ▼                                                │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ HomePage homePage = new HomePage(driver)             │           │
│  │ homePage.navigate(appUrl)                            │           │
│  │    ├─ driver.get("https://elpais.com")              │           │
│  │    ├─ waitForPageLoad() [wait for DOM ready]         │           │
│  │    └─ closeCookieBanner()                            │           │
│  │         ├─ Wait 5 sec for cookie button              │           │
│  │         └─ IF button visible: click it               │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     ▼                                                │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ OpinionPage opinionPage =                            │           │
│  │   homePage.navigateToOpinionSection()                │           │
│  │    ├─ Click "Opinión" link                           │           │
│  │    │  (Or use hamburger menu if hidden)              │           │
│  │    └─ Wait for Opinion page to load                  │           │
│  │    └─ Return OpinionPage instance                    │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     ▼                                                │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ opinionPage.waitForArticlesLoad()                    │           │
│  │    └─ Wait for article elements to appear            │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     ▼                                                │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ List<ArticleData> articles =                         │           │
│  │   opinionPage.getTopArticles(5)                      │           │
│  │                                                       │           │
│  │ FOR i = 0 to 4:                                      │           │
│  │   ├─ Scroll to article[i] in viewport                │           │
│  │   ├─ Wait 2 seconds [images lazy-load]               │           │
│  │   ├─ Extract title: "Spanish Article Title"          │           │
│  │   ├─ Extract content: "Spanish article description"  │           │
│  │   ├─ Extract image URL: "https://elpais.com/img.jpg" │           │
│  │   ├─ Extract article URL: "https://elpais.com/art"   │           │
│  │   └─ articles.add(new ArticleData(...))              │           │
│  │                                                       │           │
│  │ Return: [ArticleData1, ArticleData2, ...]            │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     ▼                                                │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Assert test conditions:                              │           │
│  │  assert !articles.isEmpty()                          │           │
│  │  Logger.info("Successfully scraped 5 articles")      │           │
│  └──────────────────┬───────────────────────────────────┘           │
│                     │                                                │
│                     ▼                                                │
│  TEST PASSED ✓                                                       │
│                                                                      │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│         5. @AfterMethod: tearDown() EXECUTED                        │
│                                                                      │
│  DriverManager.quitDriver()                                         │
│    ├─ Get driver from ThreadLocal                                   │
│    ├─ driver.quit()  [Close browser]                                │
│    └─ threadLocalDriver.remove()  [Clean up]                        │
│                                                                      │
│  Result: Browser closed, resources freed                            │
│                                                                      │
└────────────────────┬────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              6. TEST RESULTS GENERATED                               │
│  ├─ testng-results.xml (machine readable)                           │
│  ├─ console logs (human readable)                                   │
│  ├─ ./logs/browserstack_test.log (detailed log4j)                   │
│  └─ Test report created                                             │
│                                                                      │
│  Output:
│  Tests run: 2, Failures: 0, Skipped: 0                              │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## ARTICLE SCRAPING FLOW (DETAILED)

```
OpinionPage.getTopArticles(5)
│
├─ waitForArticlesLoad()
│  │
│  └─ wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_ITEMS))
│     └─ Waits for: //article[contains(@class, 'c c-o')]
│
├─ List<WebElement> articles = getElements(ARTICLE_ITEMS)
│  │
│  └─ Returns: [Article_0, Article_1, Article_2, ...]
│
└─ FOR LOOP: for (int i = 0; i < Math.min(5, articles.size()); i++)
   │
   ├─ Iteration 0:
   │  │
   │  ├─ WebElement article = articles.get(0)
   │  │
   │  ├─ scrollToElement(article)
   │  │  └─ js.executeScript("arguments[0].scrollIntoView({block:'center'});", article)
   │  │
   │  ├─ Thread.sleep(2000)  [Wait for images to load]
   │  │
   │  ├─ Try to extract title:
   │  │  │
   │  │  ├─ Try: article.findElement(ARTICLE_TITLE).getText()
   │  │  │  └─ Locator: By.cssSelector("h2.c_t, h2[class*='c_t'], h2 a")
   │  │  │
   │  │  └─ Fallback: article.findElement(By.tagName("h2")).getText()
   │  │     └─ Result: "El cambio climático afecta nuestro futuro"
   │  │
   │  ├─ Try to extract content:
   │  │  │
   │  │  ├─ Try: article.findElement(ARTICLE_CONTENT).getText()
   │  │  │  └─ Locator: By.cssSelector("p.c_d, p[class*='c_d']")
   │  │  │
   │  │  └─ Result: "Spanish article description text..."
   │  │
   │  ├─ Try to extract image:
   │  │  │
   │  │  ├─ Find image element:
   │  │  │  ├─ Try: article.findElement(ARTICLE_IMAGE)
   │  │  │  │  └─ Locator: By.cssSelector("img.c_m_e, img")
   │  │  │  │
   │  │  │  └─ Get URL from attributes (in order):
   │  │  │     ├─ src="https://elpais.com/img.jpg"  (standard)
   │  │  │     ├─ data-src="https://elpais.com/img.jpg"  (lazy-load)
   │  │  │     └─ srcset="img-low.jpg 1x, img-high.jpg 2x"  (responsive)
   │  │  │
   │  │  └─ Normalize URL:
   │  │     └─ IF starts with "//": prepend "https:"
   │  │
   │  ├─ Try to extract article URL:
   │  │  │
   │  │  └─ article.findElement(ARTICLE_LINK).getAttribute("href")
   │  │     └─ Result: "https://elpais.com/opinion/2024/02/19/article.html"
   │  │
   │  └─ Create ArticleData object:
   │     │
   │     └─ new ArticleData(
   │          title = "El cambio climático afecta nuestro futuro",
   │          content = "Spanish description...",
   │          imageUrl = "https://elpais.com/img.jpg",
   │          articleUrl = "https://elpais.com/opinion/..."
   │        )
   │     │
   │     └─ articles.add(articleData)
   │
   ├─ Iteration 1, 2, 3, 4: [Same process]
   │
   └─ Return: List<ArticleData> with 5 articles
      │
      └─ [
           ArticleData{title: "...", content: "...", imageUrl: "...", ...},
           ArticleData{title: "...", content: "...", imageUrl: "...", ...},
           ArticleData{title: "...", content: "...", imageUrl: "...", ...},
           ArticleData{title: "...", content: "...", imageUrl: "...", ...},
           ArticleData{title: "...", content: "...", imageUrl: "...", ...}
         ]
```

---

## IMAGE DOWNLOAD FLOW

```
ImageDownloader.downloadImage(imageUrl, outputPath)
│
├─ IF imageUrl is null or empty:
│  └─ Return "" (empty string)
│
├─ Clean URL:
│  ├─ Remove query parameters: "img.jpg?resize=300" → "img.jpg"
│  └─ IF not http(s): prepend "https:"
│
├─ Create directory:
│  └─ new File(outputPath).mkdirs()
│     └─ Result: ./downloads/images/
│
├─ Generate filename:
│  ├─ From URL: "https://elpais.com/media/img_12345.jpg"
│  │  └─ Extract: "img_12345.jpg"
│  │
│  └─ IF invalid: "image_" + System.currentTimeMillis() + ".jpg"
│
├─ Open HTTP connection:
│  │
│  ├─ URL url = new URL(imageUrl)
│  ├─ URLConnection connection = url.openConnection()
│  │
│  └─ Set headers (CRITICAL):
│     ├─ User-Agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64)..."
│     ├─ Referer: "https://elpais.com/"
│     ├─ Accept: "image/avif,image/webp,image/apng,image/jpeg..."
│     ├─ Sec-Fetch-Dest: "image"
│     ├─ Sec-Fetch-Mode: "no-cors"
│     └─ Sec-Fetch-Site: "cross-site"
│     │
│     └─ These headers make CDN think it's a real browser
│
├─ Download file:
│  │
│  ├─ InputStream inputStream = connection.getInputStream()
│  ├─ FileOutputStream fileOutputStream = new FileOutputStream(filePath)
│  │
│  └─ Read & Write in chunks:
│     │
│     ├─ byte[] buffer = new byte[4096]
│     │
│     └─ WHILE (bytesRead = inputStream.read(buffer)) != -1:
│        │
│        └─ fileOutputStream.write(buffer, 0, bytesRead)
│           │
│           └─ Example:
│              ├─ Chunk 1: 4096 bytes written
│              ├─ Chunk 2: 4096 bytes written
│              ├─ Chunk 3: 4096 bytes written
│              └─ Chunk 4: 2048 bytes written (last chunk, smaller)
│
├─ Log result:
│  └─ logger.info("Image downloaded successfully: {}", filePath)
│
└─ Return: "./downloads/images/img_12345.jpg"
   │
   └─ File successfully saved on disk
```

---

## TRANSLATION FLOW WITH CACHING

```
TranslationService.translateToEnglish("El cambio climático afecta nuestro futuro")
│
├─ Step 1: Check cache
│  │
│  ├─ String cacheKey = "es-en-El cambio climático afecta nuestro futuro"
│  │
│  ├─ IF translationCache.containsKey(cacheKey):
│  │  │
│  │  └─ RETURN translationCache.get(cacheKey)  [FAST - from memory]
│  │     └─ "The climate change affects our future"
│  │
│  └─ ELSE: Continue to step 2
│
├─ Step 2: Prepare API request
│  │
│  ├─ Load credentials:
│  │  ├─ apiKey = ConfigManager.get("RAPIDAPI_KEY")
│  │  │  └─ "YOUR_RAPIDAPI_KEY_HERE"
│  │  │
│  │  └─ apiHost = ConfigManager.get("RAPIDAPI_HOST")
│  │     └─ "rapid-translate-multi-traduction.p.rapidapi.com"
│  │
│  ├─ Build JSON request:
│  │  │
│  │  └─ {
│  │       "from": "es",
│  │       "to": "en",
│  │       "text": "El cambio climático afecta nuestro futuro"
│  │     }
│  │
│  └─ Create HTTP POST request:
│     │
│     └─ httpPost = new HttpPost("https://rapid-translate-multi-traduction.p.rapidapi.com/t")
│
├─ Step 3: Set API headers
│  │
│  ├─ x-rapidapi-key: "YOUR_RAPIDAPI_KEY_HERE"
│  ├─ x-rapidapi-host: "rapid-translate-multi-traduction.p.rapidapi.com"
│  └─ Content-Type: "application/json"
│
├─ Step 4: Send request & get response
│  │
│  ├─ httpClient.execute(httpPost)
│  │  │
│  │  └─ API Response: "The climate change affects our future"
│  │
│  └─ Parse response:
│     │
│     ├─ IF response is JSON string: remove quotes
│     │  └─ "..." → ...
│     │
│     └─ Result: "The climate change affects our future"
│
├─ Step 5: Cache result
│  │
│  └─ translationCache.put(cacheKey, translation)
│     │
│     └─ HashMap now contains:
│        └─ {
│             "es-en-El cambio climático afecta nuestro futuro" : 
│             "The climate change affects our future"
│           }
│
├─ Step 6: Return translation
│  │
│  └─ Return: "The climate change affects our future"
│
└─ Next call for SAME text:
   │
   └─ Returns from cache in Step 1 (much faster, no API call)
```

---

## WORD FREQUENCY ANALYSIS FLOW

```
WordFrequencyAnalyzer.analyzeFrequency(allTranslatedHeadersText)
│
├─ Input text:
│  │
│  └─ "The climate change affects our future. Climate actions needed. Change is required."
│
├─ Step 1: Normalize text
│  │
│  ├─ Convert to lowercase:
│  │  └─ "the climate change affects our future. climate actions needed. change is required."
│  │
│  ├─ Remove special characters (keep only letters & spaces):
│  │  └─ "the climate change affects our future climate actions needed change is required"
│  │
│  └─ Split into words:
│     └─ [the, climate, change, affects, our, future, climate, actions, needed, change, is, required]
│
├─ Step 2: Filter stop words & count
│  │
│  ├─ STOP_WORDS = {the, is, our, a, an, and, ...}  [100+ words]
│  │
│  └─ FOR each word in words:
│     │
│     ├─ "the": IN stop_words → SKIP
│     ├─ "climate": NOT in stop_words AND length > 2 → COUNT it
│     │  └─ frequencyMap.put("climate", 1)
│     │
│     ├─ "change": NOT in stop_words AND length > 2 → COUNT it
│     │  └─ frequencyMap.put("change", 1)
│     │
│     ├─ "affects": NOT in stop_words AND length > 2 → COUNT it
│     │  └─ frequencyMap.put("affects", 1)
│     │
│     ├─ ... (process all words)
│     │
│     ├─ "climate": (2nd occurrence) → INCREMENT
│     │  └─ frequencyMap.put("climate", 2)
│     │
│     └─ "change": (2nd occurrence) → INCREMENT
│        └─ frequencyMap.put("change", 2)
│
├─ Step 3: Build frequency map
│  │
│  └─ frequencyMap = {
│       climate: 2,
│       change: 2,
│       affects: 1,
│       future: 1,
│       actions: 1,
│       needed: 1,
│       required: 1
│     }
│
├─ Step 4: Sort by frequency (descending)
│  │
│  └─ Use LinkedHashMap to maintain order:
│     │
│     └─ {
│          climate: 2,      ← Most frequent
│          change: 2,       ← Most frequent
│          affects: 1,
│          future: 1,
│          actions: 1,
│          needed: 1,
│          required: 1
│        }
│
└─ Return: Sorted map with frequencies
   │
   └─ Use in analysis:
      │
      ├─ getTopWords(text, 3) returns:
      │  └─ [
      │      WordFrequency{word: "climate", frequency: 2},
      │      WordFrequency{word: "change", frequency: 2},
      │      WordFrequency{word: "affects", frequency: 1}
      │    ]
      │
      └─ getStatistics(text) returns:
         └─ WordFrequencyStats{
              uniqueWords: 7,
              totalOccurrences: 12,
              maxFrequency: 2,
              averageFrequency: 1.71
            }
```

---

## CONFIGURATION LOADING SEQUENCE

```
JVM Startup
│
└─ ConfigManager class loads (static initializer)
   │
   ├─ Step 1: loadConfigProperties()
   │  │
   │  ├─ Load: src/main/resources/config.properties
   │  │
   │  ├─ Read properties:
   │  │  ├─ app.url=https://elpais.com
   │  │  ├─ app.timeout.implicit=15
   │  │  ├─ rapidapi.key=RAPIDAPI_KEY
   │  │  ├─ download.path=./downloads/images
   │  │  └─ ... (other properties)
   │  │
   │  └─ Store in static Properties object
   │     └─ properties = {...all config...}
   │
   ├─ Step 2: loadDotEnv()
   │  │
   │  ├─ Look for: .env file in project root
   │  │
   │  ├─ IF .env exists:
   │  │  │
   │  │  ├─ Read file line by line:
   │  │  │  ├─ "BROWSERSTACK_USERNAME=your_username"
   │  │  │  ├─ "BROWSERSTACK_ACCESSKEY=your_access_key"
   │  │  │  ├─ "RAPIDAPI_KEY=your_api_key"
   │  │  │  └─ ... (other env vars)
   │  │  │
   │  │  ├─ Parse each line:
   │  │  │  ├─ Skip comments (lines starting with #)
   │  │  │  ├─ Split by "=" into key and value
   │  │  │  └─ properties.setProperty(key, value)
   │  │  │
   │  │  └─ Override any matching keys from config.properties
   │  │
   │  └─ IF .env NOT found:
   │     └─ Log warning, continue with config.properties only
   │
   └─ Step 3: Initialization complete
      │
      └─ properties map now contains all configuration
         │
         ├─ From config.properties: default values
         ├─ From .env: sensitive/environment-specific values
         └─ Ready for use via ConfigManager.get()

At runtime:
│
└─ Code calls: ConfigManager.get("app.url")
   │
   ├─ Look up in properties map
   ├─ IF found: return value
   ├─ IF not found: check if System.getenv() has it
   └─ Return: value or empty string
```

---

## TEST PARAMETER INHERITANCE

```
testng.xml defines parameters
│
├─ <test name="Local_Chrome">
│  │
│  ├─ <parameter name="env" value="local"/>
│  ├─ <parameter name="browserName" value="chrome"/>
│  ├─ <parameter name="browserVersion" value="latest"/>
│  ├─ <parameter name="os" value="Windows"/>
│  ├─ <parameter name="osVersion" value="11"/>
│  ├─ <parameter name="resolution" value="1920x1080"/>
│  │
│  └─ <classes>
│     └─ <class name="ArticleScrapingTest"/>
│        └─ <class name="TranslationAnalysisTest"/>
│
└─ At runtime:
   │
   ├─ TestNG looks at @BeforeMethod signature:
   │  │
   │  └─ @Parameters({"env", "browserName", "browserVersion", ...})
   │     public void setUp(
   │       @Optional("local") String env,
   │       @Optional("chrome") String browserName,
   │       @Optional("latest") String browserVersion,
   │       @Optional("Windows") String os,
   │       @Optional("11") String osVersion,
   │       @Optional("1920x1080") String resolution
   │     )
   │
   ├─ TestNG maps values:
   │  │
   │  ├─ env = "local"  (from testng.xml)
   │  ├─ browserName = "chrome"  (from testng.xml)
   │  ├─ browserVersion = "latest"  (from testng.xml)
   │  ├─ os = "Windows"  (from testng.xml)
   │  ├─ osVersion = "11"  (from testng.xml)
   │  └─ resolution = "1920x1080"  (from testng.xml)
   │
   └─ Calls: setUp("local", "chrome", "latest", "Windows", "11", "1920x1080")
      │
      └─ All parameters available in setUp() method
```

---

## THREAD SAFETY WITH THREADLOCAL

```
Multiple Tests Running in Parallel
│
├─ Test Thread 1                      │  Test Thread 2                    │  Test Thread 3
│  ├─ @BeforeMethod setUp()            │  ├─ @BeforeMethod setUp()        │  ├─ @BeforeMethod setUp()
│  │  │                                │  │  │                             │  │  │
│  │  └─ driver1 = createDriver()      │  └─ driver2 = createDriver()    │  └─ driver3 = createDriver()
│  │     │                             │     │                            │     │
│  │     └─ threadLocalDriver.set(     │     └─ threadLocalDriver.set(   │     └─ threadLocalDriver.set(
│  │        driver1)                   │        driver2)                  │        driver3)
│  │                                   │                                  │
│  ├─ @Test testArticles()             │  ├─ @Test testArticles()        │  ├─ @Test testArticles()
│  │  │                                │  │  │                             │  │  │
│  │  ├─ driver = getDriver()          │  ├─ driver = getDriver()        │  ├─ driver = getDriver()
│  │  │  │                             │  │  │                             │  │  │
│  │  │  └─ Returns: driver1           │  └─ Returns: driver2            │  └─ Returns: driver3
│  │  │                                │                                  │
│  │  └─ driver.get("elpais.com")      │  └─ driver.get("elpais.com")   │  └─ driver.get("elpais.com")
│  │     (Chrome on Windows)           │     (Chrome on Windows)          │     (Chrome on Windows)
│  │                                   │                                  │
│  ├─ @AfterMethod tearDown()          │  ├─ @AfterMethod tearDown()     │  ├─ @AfterMethod tearDown()
│  │  │                                │  │  │                             │  │  │
│  │  └─ quitDriver()                  │  └─ quitDriver()                │  └─ quitDriver()
│  │     ├─ driver = getDriver()       │     ├─ driver = getDriver()     │     ├─ driver = getDriver()
│  │     │  └─ Returns: driver1        │     │  └─ Returns: driver2      │     │  └─ Returns: driver3
│  │     ├─ driver.quit()              │     ├─ driver.quit()            │     ├─ driver.quit()
│  │     └─ threadLocal.remove()       │     └─ threadLocal.remove()     │     └─ threadLocal.remove()
│  │                                   │                                  │
│  └─ [Test 1 cleaned up]              │  └─ [Test 2 cleaned up]         │  └─ [Test 3 cleaned up]
│
Result: Each thread has completely separate driver, no interference!
```

---

## ERROR HANDLING EXAMPLE: TRANSLATION SERVICE

```
TranslationService.translateToEnglish("Some text")
│
├─ Try to translate via RapidAPI
│  │
│  └─ CloseableHttpClient httpClient = HttpClients.createDefault()
│
├─ Scenario 1: Success
│  │
│  ├─ API returns: "Translated text"
│  ├─ translationCache.put(key, translation)
│  └─ Return: "Translated text"  ✓
│
├─ Scenario 2: API Key not configured
│  │
│  ├─ IF apiKey == null OR apiKey.isEmpty():
│  │  │
│  │  ├─ logger.warn("RapidAPI Key not configured")
│  │  └─ Return: original Spanish text (graceful degradation)
│  │
│  └─ Result: Test continues with original text ✓
│
├─ Scenario 3: Network error (timeout, connection refused)
│  │
│  ├─ Exception caught in catch block
│  │  │
│  │  ├─ logger.error("RapidAPI translation error", exception)
│  │  ├─ Throw RuntimeException (caught in test)
│  │  │
│  │  └─ Test fails, but logs detailed error
│  │
│  └─ Result: Test failure is traceable ✓
│
├─ Scenario 4: API returns error response
│  │
│  ├─ Check response: responseBody.contains("error")
│  │  │
│  │  ├─ logger.warn("RapidAPI Error: {}", responseBody)
│  │  └─ Return: original Spanish text
│  │
│  └─ Result: Test continues, no crash ✓
│
└─ Finally block:
   │
   └─ httpClient.close()  [Cleanup resources]
      │
      └─ Prevents resource leak, ensures cleanup even if exception
```

---

## WAIT MECHANISMS COMPARISON

```
Scenario: Need to click "Opinion" link

Without Waits (FLAKY ❌):
│
├─ driver.findElement(By.xpath("//a[href='/opinion']")).click()
│
├─ Potential problem:
│  └─ If element not in DOM yet → NoSuchElementException
│
└─ Test fails randomly (flaky)

┌─────────────────────────────────────────────────────────┐

With Implicit Wait (Not recommended):
│
├─ driver.manage().timeouts().implicitlyWait(10, SECONDS)
│
├─ driver.findElement(By.xpath("//a[href='/opinion']")).click()
│
├─ Problem:
│  ├─ Waits 10 seconds on EVERY findElement call
│  ├─ Even if element found in 100ms, still waits rest of 10s
│  ├─ Can't customize per operation
│  └─ Makes tests slow overall
│
└─ Not efficient but technically works

┌─────────────────────────────────────────────────────────┐

With Explicit Wait (RECOMMENDED ✓):
│
├─ wait.until(ExpectedConditions.elementToBeClickable(locator))
│  │
│  └─ Waits until:
│     ├─ Element present in DOM
│     ├─ Element visible (not display:none)
│     └─ Element enabled (not disabled)
│
├─ .click()  (Element guaranteed to be clickable)
│
├─ Benefits:
│  ├─ Waits only as long as needed
│  ├─ Can customize timeout per operation
│  ├─ Clear intent: "wait for clickable"
│  └─ Much more reliable
│
└─ Test passes reliably ✓
```

---

## COMPLETE DATA FLOW: START TO END

```
START → Configuration Loading → Driver Setup → Navigate → Scrape
  ↓         (config.properties,    (WebDriver)   (Homepage)   (Articles)
  │         .env file)              Creation              
  │           │                         │            │           │
  │           │                         │            │           │
  │           ▼                         ▼            ▼           ▼
  │     ConfigManager              DriverManager   HomePage    OpinionPage
  │     ├─ app.url               (ThreadLocal)    ├─ Navigate  ├─ Get articles
  │     ├─ api keys             ├─ Chrome driver  └─ Close      ├─ Extract title
  │     └─ timeouts            └─ Setup path     cookies    ├─ Extract image
  │                                                            ├─ Extract URL
  │                                                            └─ Extract content
  │
  └─→ Download Images → Translate → Analyze Word → Log Results → Cleanup
        (ImageDownloader)  (Translation)  (Frequency)    (Log4j)      (Quit)
          │                   │              │              │           │
          ├─ Validate URL    ├─ Check cache  ├─ Normalize  ├─ Console  ├─ driver.quit()
          ├─ Set headers     ├─ Call API     ├─ Remove     │  logs    ├─ threadLocal
          ├─ Download        ├─ Cache result │  stopwords  ├─ File    │  .remove()
          └─ Save file       └─ Return text  ├─ Count freq │  logs    └─ Resources
                                              └─ Sort       └─ Metrics   freed
                                                                    
RESULTS: Articles with translations and frequency analysis logged
```

---

That's it! You now have a complete visual understanding of the project. Study these diagrams alongside the code for maximum understanding! 🎯

