package com.sun.mybookreader.mt;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.sun.mybookreader.html.LinkTagSet;
import com.sun.mybookreader.utils.Log;

public class MtParser {
	private static final String TAG = "MtParser";

	private String mHtmlUrl;
	private Parser mParser;

	public MtParser() {
		parser(MtUtils.MT_URL);
	}

	public MtParser(String url){
		parser(url);
	}

	public void parser(){
		try {
			mParser = new Parser(mHtmlUrl);
		} catch (ParserException e) {	
			e.printStackTrace();
			Log.e(TAG, "ParserException"+e.toString());
		}
	}

	public void parser(String url){
		mHtmlUrl = url;
		parser();
	}

	public List<LinkTagSet> getBookCategory(){
		//		Log.d(TAG, "start getBookCategory "+mParser);
		List<LinkTagSet> list  = new ArrayList<LinkTagSet>();
		NodeFilter filter = new HasAttributeFilter("class", "mainnav_list");

		//		try {
		//			for (NodeIterator i = mParser.elements (); i.hasMoreNodes(); ) {
		//				Node node = i.nextNode();
		//				Log.d(TAG, "html = "+node.toHtml());
		//			}
		//		} catch (ParserException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() < 1){
				Log.d(TAG, "getBookCategory = null");
				return null;
			}
			NodeList nodes2 = nodes.elementAt(0).getChildren();
			for (int i = 0; i < nodes2.size(); i++){
				Node n = nodes2.elementAt(i);
				if(n instanceof LinkTag){
					LinkTag link = (LinkTag)n;
					Log.d(TAG, " text : "+ link.toPlainTextString()+"   "+link.getLink());
					if(!MtUtils.MT_URL.equals(link.getLink()) && ! "/".equals(link.getLink()))
						list.add(new LinkTagSet(link.getLink(), link.toPlainTextString()));
				}
			};

		} catch (ParserException e) {
			Log.e(TAG, "getBookCategory ParserException");
			e.printStackTrace();
		}

		return list;
	}

	public List<MtBookUtil> getBookList(String url){
		//		Log.d(TAG, "open url = "+url);
		List<MtBookUtil> list = new ArrayList<MtBookUtil>();
		parser(url);
		NodeFilter filter = new HasAttributeFilter("class", "tutui");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			for(int i = 0; i < nodes.size(); i++){
				Node n = nodes.elementAt(i);
				if(n instanceof Div){
					Div d = (Div)n;
					MtBookUtil bl = new MtBookUtil();
					for (int j =0; j < d.getChildren().size(); j++){
						if(d.getChildren().elementAt(j).getText().contains("tutuiImg")){
							//							Log.d(TAG, "image html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							while(!(d2.elementAt(0) instanceof ImageTag)){
								d2 = d2.elementAt(0).getChildren();
							}

							ImageTag img = (ImageTag)d2.elementAt(0);
							bl.imageUrl = img.getImageURL();
						} else if(d.getChildren().elementAt(j).getText().contains("tutuiTitle")){
							//							Log.d(TAG, "title html = "+d.getChildren().elementAt(j).toHtml());
							NodeList d2 = d.getChildren().elementAt(j).getChildren();
							for(Node d3 : d2.toNodeArray()){
								if( d3 instanceof HeadingTag){
									HeadingTag d4 = ((HeadingTag) d3);
									String Tag = d4.getTagName();
									if("H1".equals(Tag)){
										LinkTag link = (LinkTag)d4.getChildren().elementAt(0);
										bl.bookUrl = link.getLink();
										bl.bookName = link.toPlainTextString();
									} else if("H2".equals(Tag)){
										bl.bookAuthor = d4.getChildren().elementAt(0).toPlainTextString();
									} else if("H3".equals(Tag)){
										bl.bookAbout = d3.getLastChild().toPlainTextString();
									}
								}
							}
						}
					}
					list.add(bl);
				}
			}

		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}

		return list;
	}

	public MtBookDetail getBookDetail(String url){
		MtBookDetail mbd = new MtBookDetail();
		parser(url);

		NodeFilter filter = new HasAttributeFilter("class", "content");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() == 0){
				return null;
			}
			NodeList n = nodes.elementAt(0).getChildren();
			for(Node d : n.toNodeArray()){
				if(d.getChildren() == null){
					continue;
				}
				for (int j =0; j < d.getChildren().size(); j++){
					String divClass = d.getChildren().elementAt(j).getText();
					if(divClass.contains("movieInfo")){
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3.getText().contains("moviePic")){
								ImageTag img = (ImageTag)d3.getFirstChild();
								mbd.imageUrl = img.getImageURL();
								Log.d(TAG, "mbd.imageUrl = "+mbd.imageUrl);
							} else if(d3.getText().contains("movieDetail")){
								int size = d3.getChildren().size();
								for(int num = 0; num <size; num++){
									if(d3.getChildren().elementAt(num) instanceof ParagraphTag){
										mbd.bookDetail += d3.getChildren().elementAt(num).toPlainTextString();
										if(num != size - 1){
											mbd.bookDetail += "\n";
										}
									}
								}

								Log.d(TAG, "mbd.bookDetail = "+mbd.bookDetail);
							}
						}
					} else if(divClass.contains("movieIntro")){
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3 instanceof ParagraphTag){
								mbd.bookAbout = d3.toPlainTextString();
								Log.d(TAG, "mbd.bookAbout = "+mbd.bookAbout);
							}
						}
					} else if(divClass.contains("book_listtext")){
						int num = 0;
						NodeList d2 = d.getChildren().elementAt(j).getChildren();
						for(Node d3 : d2.toNodeArray()){
							if(d3.getChildren() == null){
								continue;
							}
							if(d3.getFirstChild() instanceof LinkTag){
								LinkTag d4 = (LinkTag) d3.getFirstChild();
								LinkTagSet link = new LinkTagSet(d4.getLink(), d4.toPlainTextString());
								Log.d(TAG, "Bullet link = "+link.getLink() + "plaint text = "+link.getPlainTextString());
								mbd.bookChapters.put(""+num++, link);
							}
						}
					}
				}
			}

		} catch (ParserException e) {
			Log.e(TAG, "getBookList ParserException");
			e.printStackTrace();
		}

		return mbd;
	}

	public String getBookChapterContent(String url) {
		String str = "";
		parser(url);
		NodeFilter filter = new HasAttributeFilter("id", "booktext");
		try {
			NodeList nodes = mParser.extractAllNodesThatMatch(filter);
			if(nodes.size() == 0){
				return null;
			}
			NodeList n = nodes.elementAt(0).getChildren();
			for(Node d : n.toNodeArray()){
				if(d instanceof ParagraphTag){
					str = d.toPlainTextString();
				}
			}
		} catch (ParserException e) {
			Log.e(TAG, "getBookChapterContent ："+e.toString());
			e.printStackTrace();
		}
		Log.e(TAG, "getBookChapterContent ：\n"+str);
		return str;
	}
}
