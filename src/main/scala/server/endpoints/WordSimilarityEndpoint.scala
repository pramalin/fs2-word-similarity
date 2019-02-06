package com.example.http4s.blaze.demo.server.endpoints

import cats.effect.{Effect, IO, Sync, Timer}
import com.example.http4s.blaze.demo.StreamUtils
import com.example.http4s.blaze.demo.server.service.WordSimilarityService
import org.http4s._
import org.http4s.dsl.Http4sDsl
import fs2.Stream

import scala.concurrent.duration._

class WordSimilarityEndpoint[F[_]: Sync] (wordSimilarityService: WordSimilarityService[F]) extends Http4sDsl[F] {
  object Word1QueryParamMatcher extends QueryParamDecoderMatcher[String]("word1")
  object Word2QueryParamMatcher extends QueryParamDecoderMatcher[String]("word2")
  object Word3QueryParamMatcher extends QueryParamDecoderMatcher[String]("word3")
  object FactorQueryParamMatcher extends QueryParamDecoderMatcher[Double]("factor")

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "similarity" :? Word1QueryParamMatcher(a) +&
      Word2QueryParamMatcher(b) +&
      Word3QueryParamMatcher(c) +&
      FactorQueryParamMatcher(factor) =>
        Ok(wordSimilarityService.similar_words(a, b, c, factor))
  }

}
