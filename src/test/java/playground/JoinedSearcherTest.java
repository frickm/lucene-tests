package playground;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermFilter;
import org.apache.lucene.search.*;
import org.apache.lucene.search.join.FixedBitSetCachingWrapperFilter;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.google.common.base.Throwables.propagate;

public class JoinedSearcherTest extends SearcherTest {
    @Test
    public void testAddDocumentsSizes() throws Exception {
        insertDocuments();

        // get number of documents
        IndexReader reader = DirectoryReader.open(index);
        Assert.assertEquals(reader.getDocCount("key"), 6);
        Assert.assertEquals(reader.getDocCount("docType"), 2);
    }

    @Test
    public void testFilterDocumentsAndFind() throws Exception {
        insertDocuments();

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // get number of documents
        Filter f = new TermFilter(new Term("docType", "parent"));
        Query q = new TermQuery(new Term("key", "ikey1"));

        TopDocs topDocs = searcher.search(q, f, 5);
        Assert.assertEquals(1, topDocs.totalHits);
    }

    @Test
    public void testJoinQuery() throws IOException {
        insertDocuments();

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // get number of documents
        Query parentQuery = new TermQuery(new Term("docType", "parent"));
        Filter cachedFilter = new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));

        Query childQuery = new TermQuery(new Term("name", "rotatory"));
        ToParentBlockJoinQuery pjq = new ToParentBlockJoinQuery(childQuery, cachedFilter, ScoreMode.Max);

        TopDocs topDocs = searcher.search(pjq, 5);  // note that this should only return the parents
        Assert.assertEquals(2, topDocs.totalHits);

        // and the second join query that chooses only one of the two parent nodes
        Query childQuery1 = new TermQuery(new Term("name", "present"));
        ToParentBlockJoinQuery pjq1 = new ToParentBlockJoinQuery(childQuery1, cachedFilter, ScoreMode.Max);

        TopDocs topDocs1 = searcher.search(pjq1, 5);  // note that this should only return the parents
        Assert.assertEquals(1, topDocs1.totalHits);
    }

    private Iterable<Document> getDocumentsFromTopDocs(final IndexSearcher searcher, ScoreDoc[] scoreDocs) {
        return Iterables.transform(Arrays.asList(scoreDocs), new Function<ScoreDoc, Document>() {
            @Override
            public Document apply(ScoreDoc sd) {
                try {
                    return searcher.doc(sd.doc);
                } catch (IOException e) {
                    throw propagate(e);
                }
            }
        });
    }

    private void insertDocuments() throws IOException {
        ImmutableList<Document> block1 = ImmutableList.of(getDoc("ikey1", "rotatory", null),
                getDoc("ikey2", "rotatory vertigo", null),
                getDoc("ikey1", "parent1", "parent")
        );

        ImmutableList<Document> block2 = ImmutableList.of(getDoc("ikey1", "rotatory", null),
                getDoc("ikey3", "present vertigo", null),
                getDoc("ikey2", "parent2", "parent")
        );

        IndexWriter writer = indexManager.createWriter(index);
        writer.addDocuments(block1);
        writer.addDocuments(block2);
        writer.commit();
    }

}
