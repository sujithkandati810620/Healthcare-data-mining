import scrapy

class MayoClinicSpider(scrapy.Spider):
    name = "mayoclinic"


    def start_requests(self):
        start_url = 'https://connect.mayoclinic.org/group/post-covid-recovery-covid-19/?pg='
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }

        i=1
        while i<20:
            completeurl = start_url + str(i) + '#discussion-listview'
            yield scrapy.Request(url=completeurl, callback=self.parsePostsList, headers=headers)
            i+=1


    def parsePostsList(self, response):
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        disease = "Covid"
        posts = response.xpath("//div[@class='ch-activity-simple-row']")
        posts = posts[:]
        for postHeading in posts:
            postLinkAnchorTag = postHeading.xpath(".//div[@class='primary-taxonomy']/following-sibling::a/@href")
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
        postHeading = response.xpath("//h1[@class='chv4-discussion-title']/text()").get()
        postContentParas = response.xpath("//div[@class='discussion-content']/p/text()")
        postContent = ''
        for para in postContentParas:
            postContent += para.get() + ' '
        item = {
            'contentType': 'userPost',
            'postLink': postLink,
            'postHeading': postHeading,
            'postContent': postContent
        }
        yield item