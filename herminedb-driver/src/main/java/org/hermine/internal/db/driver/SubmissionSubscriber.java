package org.hermine.internal.db.driver;

import jdk.incubator.sql2.Submission;

import java.util.concurrent.Flow;

public interface SubmissionSubscriber<T> extends Submission<T>, Flow.Subscriber<T> {

}
