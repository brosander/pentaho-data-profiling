/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package com.pentaho.profiling.model;

import com.pentaho.profiling.api.Profile;
import com.pentaho.profiling.api.ProfileCreationException;
import com.pentaho.profiling.api.ProfileFactory;
import com.pentaho.profiling.api.ProfileStatus;
import com.pentaho.profiling.api.ProfilingService;
import com.pentaho.profiling.api.datasource.DataSourceReference;
import com.pentaho.profiling.api.measure.MeasureMetadata;
import com.pentaho.profiling.api.measure.RequestedMeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bryan on 7/31/14.
 */
public class ProfilingServiceImpl implements ProfilingService {
  private static final Map<String, Profile> profileMap = new HashMap<String, Profile>();
  private List<ProfileFactory> factories;

  public List<ProfileFactory> getFactories() {
    return factories;
  }

  public void setFactories( List<ProfileFactory> factories ) {
    this.factories = factories;
  }

  @Override
  public ProfileStatus create( DataSourceReference dataSourceReference ) throws ProfileCreationException {
    Profile profile = null;
    for ( ProfileFactory factory : factories ) {
      if ( factory.accepts( dataSourceReference ) ) {
        profile = factory.create( dataSourceReference );
        synchronized( profileMap ) {
          profileMap.put( profile.getId(), profile );
        }
        return profile.getProfileUpdate();
      }
    }
    return null;
  }

  @Override
  public List<MeasureMetadata> getSupportedMeasures( String profileId ) {
    synchronized( profileMap ) {
      return profileMap.get( profileId ).getSupportedMeasures();
    }
  }

  @Override
  public void setRequestedMeasures( String profileId, List<RequestedMeasure> measures ) {
    synchronized( profileMap ) {
      profileMap.get( profileId ).setRequestedMeasures( measures );
    }
  }

  @Override
  public List<ProfileStatus> getActiveProfiles() {
    synchronized( profileMap ) {
      List<ProfileStatus> result = new ArrayList<ProfileStatus>( profileMap.size() );
      for ( Profile profile : profileMap.values() ) {
        result.add( profile.getProfileUpdate() );
      }
      return result;
    }
  }

  @Override
  public ProfileStatus getProfileUpdate( String profileId ) {
    synchronized( profileMap ) {
      return profileMap.get( profileId ).getProfileUpdate();
    }
  }
}
