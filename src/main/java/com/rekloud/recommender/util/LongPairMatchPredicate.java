package com.rekloud.recommender.util;

import org.apache.mahout.cf.taste.impl.common.Cache;
import org.apache.mahout.common.LongPair;

/**
 * A {@link Cache.MatchPredicate} which will match an ID against either element of a
 * {@link LongPair}.
 */
final class LongPairMatchPredicate implements Cache.MatchPredicate<LongPair> {

	private final long id;

	LongPairMatchPredicate(long id) {
		this.id = id;
	}

	@Override
	public boolean matches(LongPair pair) {
		return pair.getFirst() == id || pair.getSecond() == id;
	}

}
