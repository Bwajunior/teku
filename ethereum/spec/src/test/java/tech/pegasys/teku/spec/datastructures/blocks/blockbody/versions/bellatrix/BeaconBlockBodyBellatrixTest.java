/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.bellatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static tech.pegasys.teku.infrastructure.async.SafeFutureAssert.safeJoin;

import java.util.function.Consumer;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.BeaconBlockBodyBuilder;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.common.AbstractBeaconBlockBodyTest;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.altair.SyncAggregate;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayload;

class BeaconBlockBodyBellatrixTest extends AbstractBeaconBlockBodyTest<BeaconBlockBodyBellatrix> {

  protected SyncAggregate syncAggregate;
  protected ExecutionPayload executionPayload;

  @BeforeEach
  void setup() {
    super.setUpBaseClass(
        SpecMilestone.BELLATRIX,
        () -> {
          syncAggregate = dataStructureUtil.randomSyncAggregate();
          executionPayload = dataStructureUtil.randomExecutionPayload();
        });
  }

  @Test
  void equalsReturnsFalseWhenExecutionPayloadIsDifferent() {
    executionPayload = dataStructureUtil.randomExecutionPayload();
    BeaconBlockBodyBellatrix testBeaconBlockBody = safeJoin(createBlockBody());

    assertNotEquals(defaultBlockBody, testBeaconBlockBody);
  }

  @Test
  @SuppressWarnings("unchecked")
  void builderShouldFailWhenOverridingBlindedSchemaWithANullSchema() {
    BeaconBlockBodyBuilderBellatrix beaconBlockBodyBuilderBellatrix =
        new BeaconBlockBodyBuilderBellatrix();
    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                beaconBlockBodyBuilderBellatrix
                    .blindedSchema(mock(BlindedBeaconBlockBodySchemaBellatrixImpl.class))
                    .schema((BeaconBlockBodySchemaBellatrixImpl) null)
                    .randaoReveal(mock(BLSSignature.class))
                    .eth1Data(mock(Eth1Data.class))
                    .graffiti(mock(Bytes32.class))
                    .attestations(mock(SszList.class))
                    .proposerSlashings(mock(SszList.class))
                    .attesterSlashings(mock(SszList.class))
                    .deposits(mock(SszList.class))
                    .voluntaryExits(mock(SszList.class))
                    .build());
    assertEquals(exception.getMessage(), "schema must be set with no blindedSchema");
  }

  @Test
  @SuppressWarnings("unchecked")
  void builderShouldFailWhenOverridingSchemaWithANullBlindedSchema() {
    BeaconBlockBodyBuilderBellatrix beaconBlockBodyBuilderBellatrix =
        new BeaconBlockBodyBuilderBellatrix();
    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                beaconBlockBodyBuilderBellatrix
                    .schema(mock(BeaconBlockBodySchemaBellatrixImpl.class))
                    .blindedSchema(null)
                    .randaoReveal(mock(BLSSignature.class))
                    .eth1Data(mock(Eth1Data.class))
                    .graffiti(mock(Bytes32.class))
                    .attestations(mock(SszList.class))
                    .proposerSlashings(mock(SszList.class))
                    .attesterSlashings(mock(SszList.class))
                    .deposits(mock(SszList.class))
                    .voluntaryExits(mock(SszList.class))
                    .build());
    assertEquals(exception.getMessage(), "blindedSchema must be set with no schema");
  }

  @Override
  protected SafeFuture<BeaconBlockBodyBellatrix> createBlockBody(
      final Consumer<BeaconBlockBodyBuilder> contentProvider) {
    return getBlockBodySchema()
        .createBlockBody(contentProvider)
        .thenApply(body -> (BeaconBlockBodyBellatrix) body);
  }

  @Override
  protected Consumer<BeaconBlockBodyBuilder> createContentProvider() {
    return super.createContentProvider()
        .andThen(
            builder ->
                builder
                    .syncAggregate(syncAggregate)
                    .executionPayload(SafeFuture.completedFuture(executionPayload)));
  }
}
