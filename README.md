# Project: Jargon-core API

### Date:
### Release Version: 4.2.1.0-SNAPSHOT
### git tag: 

#### Developer: Mike Conway - DICE

## News

Work for milestone https://github.com/DICE-UNC/jargon/milestone/13

This version of Jargon is currently targeted at Cloud Browser and REST.  There are still some features that are considered early access and may not support a full range
of use cases for general cases, and having a separate stream allows us flexibility to break API on these more advanced features, such as advanced paging and virtual collections support.

Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

### Changes

#### Remove old thumbnail code #165 

Remove old image thumbnail code that relied on specific 'lifetime library' configuration.  This will later be replaced by a more globally applicable set of tools.  Likely in the jargon-extensions package

####  Add file to string and vice versa to support cloud browser editor #166 

Add file to string and vice versa in FileSamplerService of data utils.  This allows cloud browser to turn a file into an edit pane and store edits to irods.

