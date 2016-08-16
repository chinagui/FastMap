package com.navinfo.dataservice.dao.shm;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/** 
 * @ClassName: RedisOperator
 * @author xiaoxiaowen4127
 * @date 2016年7月25日
 * @Description: RedisOperator.java
 */
public class RedisOperator{
	protected Jedis jedis;
	public RedisOperator(int dbIndex){
		this.jedis=RedisConnector.getInstance().getConnection();
		jedis.select(dbIndex);
	}
	public void close(){
		if(jedis!=null){
			jedis.close();
		}
	}


	public String set(String key, String value) {
		return jedis.set(key, value);
	}

	public String set(String key, String value, String nxxx, String expx, long time) {
		// TODO Auto-generated method stub
		return jedis.set(key, value, nxxx, expx, time);
	}

	public String set(String key, String value, String nxxx) {
		// TODO Auto-generated method stub
		return jedis.set(key, value, nxxx);
	}

	public String get(String key) {
		// TODO Auto-generated method stub
		return jedis.get(key);
	}

	public Boolean exists(String key) {
		// TODO Auto-generated method stub
		return jedis.exists(key);
	}

	public Long persist(String key) {
		// TODO Auto-generated method stub
		return jedis.persist(key);
	}

	public String type(String key) {
		// TODO Auto-generated method stub
		return jedis.type(key);
	}
	
	public Long expire(String key, int seconds) {
		// TODO Auto-generated method stub
		return jedis.expire(key, seconds);
	}
	
	public Long pexpire(String key, long milliseconds) {
		// TODO Auto-generated method stub
		return jedis.pexpire(key, milliseconds);
	}
	
	public Long expireAt(String key, long unixTime) {
		// TODO Auto-generated method stub
		return jedis.expireAt(key, unixTime);
	}
	
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		// TODO Auto-generated method stub
		return jedis.pexpireAt(key, millisecondsTimestamp);
	}
	
	public Long ttl(String key) {
		// TODO Auto-generated method stub
		return jedis.ttl(key);
	}
	public Long pttl(String key) {
		// TODO Auto-generated method stub
		return jedis.pttl(key);
	}
	public Boolean setbit(String key, long offset, boolean value) {
		// TODO Auto-generated method stub
		return jedis.setbit(key, offset, value);
	}
	public Boolean setbit(String key, long offset, String value) {
		// TODO Auto-generated method stub
		return jedis.setbit(key, offset, value);
	}
	public Boolean getbit(String key, long offset) {
		// TODO Auto-generated method stub
		return jedis.getbit(key, offset);
	}
	public Long setrange(String key, long offset, String value) {
		// TODO Auto-generated method stub
		return jedis.setrange(key, offset, value);
	}
	public String getrange(String key, long startOffset, long endOffset) {
		// TODO Auto-generated method stub
		return jedis.getrange(key, startOffset, endOffset);
	}
	public String getSet(String key, String value) {
		// TODO Auto-generated method stub
		return jedis.getSet(key, value);
	}
	public Long setnx(String key, String value) {
		// TODO Auto-generated method stub
		return jedis.setnx(key, value);
	}
	public String setex(String key, int seconds, String value) {
		// TODO Auto-generated method stub
		return jedis.setex(key, seconds, value);
	}
	public String psetex(String key, long milliseconds, String value) {
		// TODO Auto-generated method stub
		return jedis.psetex(key, milliseconds, value);
	}
	public Long decrBy(String key, long integer) {
		// TODO Auto-generated method stub
		return jedis.decrBy(key, integer);
	}
	public Long decr(String key) {
		// TODO Auto-generated method stub
		return jedis.decr(key);
	}
	public Long incrBy(String key, long integer) {
		// TODO Auto-generated method stub
		return jedis.incrBy(key, integer);
	}
	public Double incrByFloat(String key, double value) {
		// TODO Auto-generated method stub
		return jedis.incrByFloat(key, value);
	}
	public Long incr(String key) {
		// TODO Auto-generated method stub
		return jedis.incr(key);
	}
	public Long append(String key, String value) {
		// TODO Auto-generated method stub
		return jedis.append(key, value);
	}
	public String substr(String key, int start, int end) {
		// TODO Auto-generated method stub
		return jedis.substr(key, start, end);
	}
	public Long hset(String key, String field, String value) {
		// TODO Auto-generated method stub
		return jedis.hset(key, field, value);
	}
	public String hget(String key, String field) {
		// TODO Auto-generated method stub
		return jedis.hget(key, field);
	}
	public Long hsetnx(String key, String field, String value) {
		// TODO Auto-generated method stub
		return jedis.hsetnx(key, field, value);
	}
	public String hmset(String key, Map<String, String> hash) {
		// TODO Auto-generated method stub
		return jedis.hmset(key, hash);
	}
	public List<String> hmget(String key, String... fields) {
		// TODO Auto-generated method stub
		return jedis.hmget(key, fields);
	}
	public Long hincrBy(String key, String field, long value) {
		// TODO Auto-generated method stub
		return jedis.hincrBy(key, field, value);
	}
	public Double hincrByFloat(String key, String field, double value) {
		// TODO Auto-generated method stub
		return jedis.hincrByFloat(key, field, value);
	}
	public Boolean hexists(String key, String field) {
		// TODO Auto-generated method stub
		return jedis.hexists(key, field);
	}
	public Long hdel(String key, String... field) {
		// TODO Auto-generated method stub
		return jedis.hdel(key, field);
	}
	public Long hlen(String key) {
		// TODO Auto-generated method stub
		return jedis.hlen(key);
	}
	public Set<String> hkeys(String key) {
		// TODO Auto-generated method stub
		return jedis.hkeys(key);
	}
	public List<String> hvals(String key) {
		// TODO Auto-generated method stub
		return jedis.hvals(key);
	}
	public Map<String, String> hgetAll(String key) {
		// TODO Auto-generated method stub
		return jedis.hgetAll(key);
	}
	public Long rpush(String key, String... string) {
		// TODO Auto-generated method stub
		return jedis.rpush(key, string);
	}
	public Long lpush(String key, String... string) {
		// TODO Auto-generated method stub
		return jedis.lpush(key, string);
	}
	public Long llen(String key) {
		// TODO Auto-generated method stub
		return jedis.llen(key);
	}
	public List<String> lrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return jedis.lrange(key, start, end);
	}
	public String ltrim(String key, long start, long end) {
		// TODO Auto-generated method stub
		return jedis.ltrim(key, start, end);
	}
	public String lindex(String key, long index) {
		// TODO Auto-generated method stub
		return jedis.lindex(key, index);
	}
	public String lset(String key, long index, String value) {
		// TODO Auto-generated method stub
		return jedis.lset(key, index, value);
	}
	public Long lrem(String key, long count, String value) {
		// TODO Auto-generated method stub
		return jedis.lrem(key, count, value);
	}
	public String lpop(String key) {
		// TODO Auto-generated method stub
		return jedis.lpop(key);
	}
	public String rpop(String key) {
		// TODO Auto-generated method stub
		return jedis.rpop(key);
	}
	public Long sadd(String key, String... member) {
		// TODO Auto-generated method stub
		return jedis.sadd(key, member);
	}
	public Set<String> smembers(String key) {
		// TODO Auto-generated method stub
		return jedis.smembers(key);
	}
	public Long srem(String key, String... member) {
		// TODO Auto-generated method stub
		return jedis.srem(key, member);
	}
	public String spop(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> spop(String key, long count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long scard(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Boolean sismember(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public String srandmember(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> srandmember(String key, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long strlen(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zadd(String key, double score, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zadd(String key, double score, String member, ZAddParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zrem(String key, String... member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Double zincrby(String key, double score, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zrank(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zrevrank(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zcard(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Double zscore(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> sort(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> sort(String key, SortingParams sortingParameters) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zcount(String key, double min, double max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zcount(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByScore(String key, double min, double max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByScore(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zremrangeByRank(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zremrangeByScore(String key, double start, double end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zremrangeByScore(String key, String start, String end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zlexcount(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByLex(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		// TODO Auto-generated method stub
		return null;
	}
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long zremrangeByLex(String key, String min, String max) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long lpushx(String key, String... string) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long rpushx(String key, String... string) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> blpop(String arg) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> blpop(int timeout, String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> brpop(String arg) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> brpop(int timeout, String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long del(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public String echo(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long move(String key, int dbIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long bitcount(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long bitcount(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long bitpos(String key, boolean value) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long bitpos(String key, boolean value, BitPosParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<String> sscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Tuple> zscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<String> sscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Tuple> zscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long pfadd(String key, String... elements) {
		// TODO Auto-generated method stub
		return null;
	}
	public long pfcount(String key) {
		// TODO Auto-generated method stub
		return 0;
	}
	public Long geoadd(String key, double longitude, double latitude, String member) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		// TODO Auto-generated method stub
		return null;
	}
	public Double geodist(String key, String member1, String member2) {
		// TODO Auto-generated method stub
		return null;
	}
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<String> geohash(String key, String... members) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<GeoCoordinate> geopos(String key, String... members) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}
}
