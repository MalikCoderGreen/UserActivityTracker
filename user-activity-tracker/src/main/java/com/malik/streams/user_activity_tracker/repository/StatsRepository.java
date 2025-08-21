package com.malik.streams.user_activity_tracker.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.malik.streams.user_activity_tracker.model.UserActivityEvent;
import org.springframework.stereotype.Repository;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.*;

@Repository
public class StatsRepository {

    private final ElasticsearchClient esClient;

    public StatsRepository(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public Map<String, Long> queryTopPages(String window) {
        try {

            SearchResponse<UserActivityEvent> response = esClient.search(s -> s
                            .index("user-activity")
                            .query(q -> q
                                    .range(r -> r
                                            .field("timestamp")
                                            .gte(JsonData.of("now-" + window))
                                            .lte(JsonData.of("now"))
                                    )
                            )
                            .aggregations("top_pages", a -> a
                                    .terms(t -> t
                                            .field("page.keyword")
                                            .size(10)
                                    )
                            ),
                    UserActivityEvent.class
            );

            // parse results
            Map<String, Long> results = new HashMap<>();
            response.aggregations()
                    .get("top_pages")
                    .sterms()
                    .buckets()
                    .array()
                    .forEach(bucket -> results.put(bucket.key().stringValue(), bucket.docCount()));

            return results;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
        ES query
        {
          "size": 0,
          "query": {
            "range": {
              "timestamp": { "gte": "now-24h" }
            }
          },
          "aggs": {
            "top_pages": {
              "terms": { "field": "page.keyword", "size": 10 }
            }
          }
        }
         */
    }

    public Long queryActiveUsers(String window) {
        try {

            /*
            ES query
             {
              "size": 0,
              "query": {
                "range": {
                  "timestamp": { "gte": "now-24h" }
                }
              },
              "aggs": {
                "unique_users": {
                  "cardinality": { "field": "userId.keyword" }
                }
              }
            }
         */
            SearchResponse<UserActivityEvent> response = esClient.search(s -> s
                            .index("user-activity")
                            .query(q -> q
                                    .range(r -> r
                                            .field("timestamp")
                                            .gte(JsonData.of("now-" + window))
                                            .lte(JsonData.of("now"))
                                    )
                            )
                            .aggregations("active_users", a -> a
                                    .cardinality(c -> c
                                            .field("userId.keyword")
                                    )
                            ),
                    UserActivityEvent.class
            );

            // parse results
            Long result = response.aggregations()
                    .get("active_users")
                    .cardinality()
                    .value();

            return result;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Long> queryEventsHourly(String window) {
        // TODO: ES date_histogram agg
        /*
        {
          "size": 0,
          "query": {
            "range": {
              "timestamp": { "gte": "now-24h" }
            }
          },
          "aggs": {
            "events_per_hour": {
              "date_histogram": {
                "field": "timestamp",
                "calendar_interval": "hour"
              }
            }
          }
        }
         */

        try {
            SearchResponse<UserActivityEvent> response = esClient.search(s -> s
                            .index("user-activity")
                            .size(0)
                            .query(q -> q.range(r -> r
                                    .field("timestamp")
                                    .gte(JsonData.of("now-" + window))
                                    .lte(JsonData.of("now"))
                            ))
                            .aggregations("events_per_hour", a -> a
                                    .dateHistogram(h -> h
                                            .field("timestamp")
                                            .fixedInterval(f -> f.time("1h"))
                                    )
                            ),
                    UserActivityEvent.class
            );

            Map<String, Long> results = new LinkedHashMap<>();
            response.aggregations()
                    .get("events_per_hour")
                    .dateHistogram()
                    .buckets()
                    .array()
                    .forEach(bucket -> results.put(
                            bucket.keyAsString(),
                            bucket.docCount()
                    ));

            return results;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
