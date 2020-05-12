import React, {useEffect, useState} from 'react';
import PerfectScrollbar from 'react-perfect-scrollbar';
import {
  Box,
  Drawer,
  IconButton,
  SvgIcon,
  makeStyles
} from '@material-ui/core';
import {
  XCircle as XIcon
} from 'react-feather';
import {useSelector, useDispatch} from "react-redux";
import {visualGraphActions} from "../../../actions";
import {useLazyQuery, useQuery} from "@apollo/react-hooks";
import {entityHubQueries} from "../../../graphql";

const useStyles = makeStyles(() => ({
  drawer: {
    width: 340,
    maxWidth: '100%'
  }
}));

function NodeInfoDrawer() {
  const classes = useStyles();
  const [properties, setProperties] = useState([]);
  const dispatch = useDispatch();
  const {projectId} = useSelector(state => state.projectReducer);
  const {nodeInfoDrawerOpen, selectedNode} = useSelector(state => state.visualGraphReducer);

  const [loadPredicatesFromNode] = useLazyQuery(
    entityHubQueries.predicatesFromNode, {
      onCompleted: data => {
        setProperties(data.predicatesFromNode);
      },
      fetchPolicy: 'no-cache'
    });

  useEffect(() => {
    if (selectedNode) {
      loadPredicatesFromNode({
        variables: {
          projectId,
          uri: selectedNode,
          nodeType: "literal"
        }
      })
    }
  }, [selectedNode])

  const handleClose = () => {
    dispatch(visualGraphActions.closeNodeInfoDrawer())
  };


  if (!selectedNode) return null;

  return (
    <Drawer
      anchor="right"
      classes={{paper: classes.drawer}}
      ModalProps={{BackdropProps: {invisible: true}}}
      onClose={handleClose}
      open={nodeInfoDrawerOpen}
      variant="temporary"
      >
      <PerfectScrollbar options={{suppressScrollX: true}}
                        style={{pointerEvents: 'all'}}>
        <Box p={3}>
          <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center">
            <Box style={{flexGrow: 1}}/>
            <IconButton onClick={handleClose}>
              <SvgIcon fontSize="small">
                <XIcon/>
              </SvgIcon>
            </IconButton>
          </Box>
        </Box>
        {properties.map(p => <Box px={2} py={1}>
          {p.to.value}
        </Box>)}
      </PerfectScrollbar>
    </Drawer>
  );
}

export default NodeInfoDrawer;