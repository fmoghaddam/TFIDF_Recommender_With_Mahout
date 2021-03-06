package com.rekloud.recommender.util;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.Cache;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.common.Retriever;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.LongPair;
import com.google.common.base.Preconditions;

/**
 * Caches the results from an underlying {@link ItemSimilarity} implementation.
 */
public final class CachingItemSimilarity implements ItemSimilarity {

  private final ItemSimilarity similarity;
  private final Cache<LongPair,Double> similarityCache;
  private final RefreshHelper refreshHelper;

  /**
   * Creates this on top of the given {@link ItemSimilarity}.
   * The cache is sized according to properties of the given {@link DataModel}.
   */
  public CachingItemSimilarity(ItemSimilarity similarity, DataModel dataModel) throws TasteException {
    this(similarity, dataModel.getNumItems());
  }

  /**
   * Creates this on top of the given {@link ItemSimilarity}.
   * The cache size is capped by the given size.
   */
  public CachingItemSimilarity(ItemSimilarity similarity, int maxCacheSize) {
    Preconditions.checkArgument(similarity != null, "similarity is null");
    this.similarity = similarity;
    this.similarityCache = new Cache<LongPair,Double>(new SimilarityRetriever(similarity), maxCacheSize);
    this.refreshHelper = new RefreshHelper(new Callable<Void>() {
      @Override
      public Void call() {
        //similarityCache.clear();
        return null;
      }
    });
    refreshHelper.addDependency(similarity);
  }
  
  @Override
  public double itemSimilarity(long itemID1, long itemID2) throws TasteException {
    LongPair key = itemID1 < itemID2 ? new LongPair(itemID1, itemID2) : new LongPair(itemID2, itemID1);
    return similarityCache.get(key);
  }

  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s) throws TasteException {
    int length = itemID2s.length;
    double[] result = new double[length];
    for (int i = 0; i < length; i++) {
      result[i] = itemSimilarity(itemID1, itemID2s[i]);
    }
    return result;
  }

  @Override
  public long[] allSimilarItemIDs(long itemID) throws TasteException {
    return similarity.allSimilarItemIDs(itemID);
  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    refreshHelper.refresh(alreadyRefreshed);
  }

  public void clearCacheForItem(long itemID) {
    similarityCache.removeKeysMatching(new LongPairMatchPredicate(itemID));
  }
  
  private static final class SimilarityRetriever implements Retriever<LongPair,Double> {
    private final ItemSimilarity similarity;
    
    private SimilarityRetriever(ItemSimilarity similarity) {
      this.similarity = similarity;
    }
    
    @Override
    public Double get(LongPair key) throws TasteException {
      return similarity.itemSimilarity(key.getFirst(), key.getSecond());
    }
  }

}
