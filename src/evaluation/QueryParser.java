package evaluation;

import java.io.FileNotFoundException;
import indexation.Parser;

public abstract class QueryParser extends Parser{

	private static final long serialVersionUID = 1L;

	public QueryParser(String begin) {
		super(begin);
	}

	public abstract Query nextQuery() throws FileNotFoundException;
}
