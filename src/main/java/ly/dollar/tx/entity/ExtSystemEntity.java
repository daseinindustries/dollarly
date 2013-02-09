package ly.dollar.tx.entity;

import java.util.Date;

public abstract class ExtSystemEntity
{
	protected ExtSystem extSystem;
	protected String extSystemId;
	protected String extSystemUserName;
	protected Date extSystemDate;
	protected String extSystemNotes;
	protected Date createdOn = new Date();

	public ExtSystem getExtSystem()
	{
		return extSystem;
	}

	public void setExtSystem(ExtSystem extSystem)
	{
		this.extSystem = extSystem;
	}

	public String getExtSystemId()
	{
		return extSystemId;
	}

	public void setExtSystemId(String extSystemId)
	{
		this.extSystemId = extSystemId;
	}

	public String getExtSystemUserName()
	{
		return extSystemUserName;
	}

	public void setExtSystemUserName(String extSystemUserName)
	{
		this.extSystemUserName = extSystemUserName;
	}

	public Date getExtSystemDate()
	{
		return extSystemDate;
	}

	public void setExtSystemDate(Date extSystemDate)
	{
		this.extSystemDate = extSystemDate;
	}

	public String getExtSystemNotes()
	{
		return extSystemNotes;
	}

	public void setExtSystemNotes(String extSystemNotes)
	{
		this.extSystemNotes = extSystemNotes;
	}

	public Date getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(Date createdOn)
	{
		this.createdOn = createdOn;
	}

}
