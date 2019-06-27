package gov.nih.nlm.semmed.model;

import java.util.Iterator;

import org.jdom.Element;

/**
 * @author jinho
 *
 */
public class OpasiArticle extends SemMedDocument
{
	// private static Log log = LogFactory.getLog(PubmedArticle.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String				pubDate;
	private String				authorList;

	public OpasiArticle()
	{
		abstractText = "";
	}

	/**
	 * 
	 */
	public OpasiArticle(String id, String titleText, String abstractText,
			String pubDate, boolean include)
	{
		this.id = id;
		this.titleText = titleText;
		this.abstractText = abstractText;
		this.pubDate = pubDate;
		this.include = include;
	}

	public OpasiArticle(Element e)
	{
		this.id = e.getChild("GRANT").getChildTextTrim("ID");
		Element article = e.getChild("GRANT").getChild("Article");
		this.titleText = article.getChildTextTrim("ArticleTitle");

		String absText = "";
		try
		{
			absText = article.getChild("Abstract").getChildTextTrim(
					"AbstractText");
		}
		catch (Exception le)
		{
			try
			{
				absText = e.getChild("MedlineCitation").getChild(
						"OtherAbstract").getChildTextTrim("AbstractText");
			}
			catch (Exception se)
			{
			}
		}
		this.abstractText = absText;

		String pubDate = "";
		Element pubDateNode = null;

		// TODO can we do this without raising an exception? [Alejandro]
		try
		{
			pubDateNode = article.getChild("Journal").getChild("JournalIssue")
					.getChild("PubDate");
		}
		catch (Exception le)
		{
			try
			{
				pubDateNode = article.getChild("Book").getChild("PubDate");
			}
			catch (Exception se)
			{
			}
		}

		// TODO Asuming pubDateNode is not null... [Alejandro]
		Iterator dateIter = pubDateNode.getChildren().iterator();
		while (dateIter.hasNext())
		{
			Element dateElement = (Element) dateIter.next();
			pubDate += dateElement.getTextTrim() + " ";
		}
		this.pubDate = pubDate.trim();
		this.include = true;
	}

	public void setAuthorList(String s)
	{
		authorList = s;
	}

	public String getAuthorList()
	{
		return authorList;
	}

	/**
	 * @return Returns the pubDate.
	 */
	public String getPubDate()
	{
		return pubDate;
	}

	/**
	 * @param pubDate
	 *            The pubDate to set.
	 */
	public void setPubDate(String pubDate)
	{
		this.pubDate = pubDate;
	}

	public String toString()
	{
		return "PMID- " + getId() + "\nDP  - " + getPubDate() + "\nTI  - "
				+ getTitleText() + "\nAB  - " + getAbstractText();
	}

	public Element toXml()
	{
		Element pubmedArticleNode = new Element("PubmedArticle");
		Element medlineCitationNode = new Element("MedlineCitation");
		Element pmidNode = new Element("PMID");
		pmidNode.setText(getId());
		Element articleNode = new Element("Article");
		Element titleNode = new Element("ArticleTitle");
		titleNode.setText(getTitleText());
		Element abstractNode = new Element("Abstract");
		Element abstractTextNode = new Element("AbstractText");
		abstractTextNode.setText(getAbstractText());
		abstractNode.addContent(abstractTextNode);
		articleNode.addContent(titleNode);
		articleNode.addContent(abstractNode);
		Element journalNode = new Element("Journal");
		Element journalIssueNode = new Element("JournalIssue");
		Element pubDateNode = new Element("PubDate");
		Element medlineDateNode = new Element("MedlineDate");
		medlineDateNode.setText(getPubDate());
		pubDateNode.addContent(medlineDateNode);
		journalIssueNode.addContent(pubDateNode);
		journalNode.addContent(journalIssueNode);
		articleNode.addContent(journalNode);
		medlineCitationNode.addContent(pmidNode);
		medlineCitationNode.addContent(articleNode);
		pubmedArticleNode.addContent(medlineCitationNode);

		return pubmedArticleNode;
	}

	// TODO Override getHash method [Alejandro]
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof PubmedArticle))
			return false;

		PubmedArticle co = (PubmedArticle) o;

		return (id == null ? co.id == null : id.equals(co.id))
				&& (titleText == null ? co.titleText == null : titleText
						.equals(co.titleText))
				&& (abstractText == null ? co.abstractText == null
						: abstractText.equals(co.abstractText));
	}

}
