package playground;

import com.google.common.base.Throwables;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

public class JoinedSearcher {
    private static final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

    public IndexWriter createWriter(Directory index) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        IndexWriter w = new IndexWriter(index, config);

        return w;
    }

    public void addDocuments(IndexWriter w, Iterable<Document> docs) {
        try {
            w.addDocuments(docs, analyzer);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
