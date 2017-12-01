/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spanner;

import com.google.cloud.Timestamp;
import com.google.common.collect.ImmutableMap;
import com.google.spanner.v1.Transaction;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;

import java.util.Map;

/**
 * Utility methods for tracing.
 */
class TraceUtil {
  
  static Map<String, AttributeValue> getTransactionAnnotations(Transaction t) {
    return ImmutableMap.of("Id",
        AttributeValue.stringAttributeValue(t.getId().toStringUtf8()),
        "Timestamp",
        AttributeValue.stringAttributeValue(Timestamp.fromProto(t.getReadTimestamp()).toString()));
  }
  
  static ImmutableMap<String, AttributeValue> getExceptionAnnotations(RuntimeException e) {
    if (e instanceof SpannerException) {
    return ImmutableMap.of("Status",
        AttributeValue.stringAttributeValue(((SpannerException) e).getErrorCode().toString()));
    }
    return ImmutableMap.of();
  }
  
  static ImmutableMap<String, AttributeValue> getExceptionAnnotations(SpannerException e) {
    return ImmutableMap.of("Status",
        AttributeValue.stringAttributeValue(e.getErrorCode().toString()));
  }
  
  static void endSpanWithFailure(Span span, Exception e) {
    if (e instanceof SpannerException) {
      endSpanWithFailure(span, (SpannerException) e); 
    } else {
      span.end(EndSpanOptions.builder()
          .setStatus(Status.INTERNAL.withDescription(e.getMessage()))
          .build());
    }
  }
  
  static void endSpanWithFailure(Span span, SpannerException e) {
    span.end(EndSpanOptions.builder()
        .setStatus(getOpenCensusStatus(e.getErrorCode()).withDescription(e.getMessage()))
        .build());
  }
  
  private static Status getOpenCensusStatus(ErrorCode code) {
    switch (code) {
      case ABORTED:
        return Status.ABORTED;
      case ALREADY_EXISTS:
        return Status.ALREADY_EXISTS;
      case CANCELLED:
        return Status.CANCELLED;
      case DATA_LOSS:
        return Status.DATA_LOSS;
      case DEADLINE_EXCEEDED:
        return Status.DEADLINE_EXCEEDED;
      case FAILED_PRECONDITION:
        return Status.FAILED_PRECONDITION;
      case INTERNAL:
        return Status.INTERNAL;
      case INVALID_ARGUMENT:
        return Status.INVALID_ARGUMENT;
      case NOT_FOUND:
        return Status.NOT_FOUND;
      case OUT_OF_RANGE:
        return Status.OUT_OF_RANGE;
      case PERMISSION_DENIED:
        return Status.PERMISSION_DENIED;
      case RESOURCE_EXHAUSTED:
        return Status.RESOURCE_EXHAUSTED;
      case UNAUTHENTICATED:
        return Status.UNAUTHENTICATED;
      case UNAVAILABLE:
        return Status.UNAVAILABLE;
      case UNIMPLEMENTED:
        return Status.UNIMPLEMENTED;
      case UNKNOWN:
      default:
        return Status.UNKNOWN;
    }
  }

}