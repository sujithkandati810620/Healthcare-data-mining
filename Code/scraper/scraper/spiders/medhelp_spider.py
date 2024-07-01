import scrapy

class MedHelpSpider(scrapy.Spider):
    name = "medhelp"


    def start_requests(self):
        start_url = 'https://www.medhelp.org/forums/COVID19/show/2203?page='
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }

        i=1
        while i<15:
            completeurl = start_url + str(i)
            yield scrapy.Request(url=completeurl, callback=self.parsePostsList, headers=headers)
            i+=1


    def parsePostsList(self, response):
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        disease = "Covid"
        posts = response.xpath("//div[@class='subj_entry']")
        posts = posts[:]
        for postHeading in posts:
            postLinkAnchorTag = postHeading.xpath(".//div[@class='subj_info']/h2[@class='subj_title ']/a/@href")
            postLink = response.urljoin(postLinkAnchorTag.get())
            mydict = {
                'contentType': 'disease',
                'disease': disease,
                'postLink': postLink
            }
            yield mydict
            yield scrapy.Request(url=postLink, callback=self.parsePost, headers=headers)

    def parsePost(self, response):
        postLink = response.url
        postHeading = response.xpath("//h1[@class='subj_title']/text()").get()
        postContentParas = response.xpath("//div[@id='subject_msg']/text()").getall()
        postHeading = postHeading.strip() if postHeading else None
        postContent = ' '.join(para.strip() for para in postContentParas if para.strip())

        item = {
            'contentType': 'userPost',
            'postLink': postLink,
            'postHeading': postHeading,
            'postContent': postContent
        }
        yield item