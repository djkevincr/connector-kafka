/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.kafka.nativeimpl.functions.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.kafka.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;

/**
 * Native function ballerina.net.kafka:assign which assign set of partitions to consumer.
 */
@BallerinaFunction(packageName = "ballerina.net.kafka",
        functionName = "assign",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "KafkaConsumer",
                structPackage = "ballerina.net.kafka"),
        args = {
                @Argument(name = "c",
                        type = TypeKind.STRUCT, structType = "KafkaConsumer",
                        structPackage = "ballerina.net.kafka"),
                @Argument(name = "partitions", type = TypeKind.ARRAY, elementType = TypeKind.STRUCT,
                        structType = "TopicPartition", structPackage = "ballerina.net.kafka")
        },
        returnType = { @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true)
public class Assign extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct consumerStruct = (BStruct) getRefArgument(context, 0);
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerStruct
                .getNativeData(Constants.NATIVE_CONSUMER);
        if (kafkaConsumer == null) {
            throw new BallerinaException("Kafka Consumer has not been initialized properly.");
        }

        BRefValueArray partitions = ((BRefValueArray) getRefArgument(context, 1));
        ArrayList<TopicPartition> partitionList = new ArrayList<TopicPartition>();

        if (partitions != null) {
            for (int counter = 0; counter < partitions.size(); counter++) {
                BStruct partition = (BStruct) partitions.get(counter);
                String topic = partition.getStringField(0);
                int partitionValue = new Long(partition.getIntField(0)).intValue();
                partitionList.add(new TopicPartition(topic, partitionValue));
            }
        }

        try {
            kafkaConsumer.assign(partitionList);
        } catch (IllegalArgumentException |
                IllegalStateException | KafkaException e) {
            return getBValues(BLangVMErrors.createError(context, 0, e.getMessage()));
        }

        return VOID_RETURN;
    }

}
