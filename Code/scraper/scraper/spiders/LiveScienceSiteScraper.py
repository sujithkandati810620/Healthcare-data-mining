import scrapy
from scrapy.crawler import CrawlerProcess

class ForumSpider(scrapy.Spider):
    name = "livescience_forum"

    def start_requests(self):
      
        url = 'https://forums.livescience.com/forums/coronavirus-epidemiology.42/'
        yield scrapy.Request(url=url, callback=self.parse)

    def parse(self, response):
        
        threads = response.xpath("//div[contains(@class, 'structItem-title')]/a")
        for thread in threads:
            link = response.urljoin(thread.xpath(".//@href").get())
            yield scrapy.Request(link, callback=self.parse_thread)

       
        next_page = response.xpath("//a[@class='pageNav-jump--next']/@href")
        if next_page:
            next_page_url = response.urljoin(next_page.get())
            yield scrapy.Request(next_page_url, callback=self.parse)

    def parse_thread(self, response):
       
        post_heading = response.xpath("//h1/text()").get()

        
        post_contents = response.xpath("//div[@class='bbWrapper']/text()")
        clean_post_content = ' '.join([content.strip() for content in post_contents.extract() if content.strip()])

        yield {
            'disease': 'Coronavirus Epidemiology',
            'postLink': response.url,
            'postHeading': post_heading,
            'postContent': clean_post_content
        }

def run_spider():
    
    process = CrawlerProcess({
        'LOG_LEVEL': 'ERROR',
        'FEED_FORMAT': 'csv',
        'FEED_URI': 'output.csv'
    })

    process.crawl(ForumSpider)
    process.start()


if __name__ == '__main__':
    run_spider()
