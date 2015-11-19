/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package reactor.rx.action;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.support.Bounded;
import reactor.core.support.Publishable;
import reactor.core.support.Subscribable;
import reactor.fn.Consumer;
import reactor.rx.Stream;

/**
 * Create a Processor decorated with Stream API
 *
 * @author Stephane Maldini
 * @since 2.0, 2.1
 */
public class ProcessorAction<E, O> extends Stream<O> implements Processor<E, O>, Publishable<O>, Subscribable<E> {

	protected final Subscriber<E> receiver;
	protected final Publisher<O> publisher;

	/**
	 *
	 * @param processor
	 * @param <E>
	 * @param <O>
	 * @return
	 */
	public static <E, O> ProcessorAction<E, O> create(Processor<E, O> processor){
		return create(processor, processor);
	}

	/**
	 *
	 * @param receiver
	 * @param publisher
	 * @param <E>
	 * @param <O>
	 * @return
	 */
	public static <E, O> ProcessorAction<E, O> create(Subscriber<E> receiver, Publisher<O> publisher){
		return new ProcessorAction<>(receiver, publisher);
	}

	protected ProcessorAction(Subscriber<E> receiver, Publisher<O> publisher) {
		this.receiver = receiver;
		this.publisher = publisher;
	}


	/**
	 * Create a consumer that broadcast complete signal from any accepted value.
	 *
	 * @return a new {@link Consumer} ready to forward complete signal to this stream
	 * @since 2.0
	 */
	public final Consumer<?> toCompleteConsumer() {
		return new Consumer<Object>() {
			@Override
			public void accept(Object o) {
				onComplete();
			}
		};
	}


	/**
	 * Create a consumer that broadcast next signal from accepted values.
	 *
	 * @return a new {@link Consumer} ready to forward values to this stream
	 * @since 2.0
	 */
	public final Consumer<E> toNextConsumer() {
		return new Consumer<E>() {
			@Override
			public void accept(E o) {
				onNext(o);
			}
		};
	}

	/**
	 * Create a consumer that broadcast error signal from any accepted value.
	 *
	 * @return a new {@link Consumer} ready to forward error to this stream
	 * @since 2.0
	 */
	public final Consumer<Throwable> toErrorConsumer() {
		return new Consumer<Throwable>() {
			@Override
			public void accept(Throwable o) {
				onError(o);
			}
		};
	}

	@Override
	public Subscriber<E> downstream() {
		return receiver;
	}

	@Override
	public Publisher<O> upstream() {
		return publisher;
	}

	@Override
	public void subscribe(Subscriber<? super O> s) {
		publisher.subscribe(s);
	}

	@Override
	public void onSubscribe(Subscription s) {
		receiver.onSubscribe(s);
	}

	@Override
	public void onNext(E e) {
		receiver.onNext(e);
	}

	@Override
	public void onError(Throwable t) {
		receiver.onError(t);
	}

	@Override
	public void onComplete() {
		receiver.onComplete();
	}


	@Override
	public boolean isExposedToOverflow(Bounded parentPublisher) {
		return Bounded.class.isAssignableFrom(publisher.getClass()) && ((Bounded) publisher).isExposedToOverflow(
				parentPublisher);
	}

	@Override
	public long getCapacity() {
		return Bounded.class.isAssignableFrom(publisher.getClass()) ? ((Bounded) publisher).getCapacity() : Long.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "ProcessorAction{" +
				"receiver=" + receiver +
				", publisher=" + publisher +
				'}';
	}
}
