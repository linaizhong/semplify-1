import {combineReducers} from 'redux';
import {reducer as formReducer} from 'redux-form';
import {projectReducer} from "./projectReducer";
import {cyReducer} from "./cyReducer";
import {visualGraphReducer} from "./visualGraphReducer";
import {webCrawlerReducer} from "./webCrawlerReducer";
import {yasqeReducer} from "./yasqeReducer";
import {sparqlReducer} from "./sparqlReducer";

const rootReducer = combineReducers({
  form: formReducer,
  projectReducer,
  cyReducer,
  visualGraphReducer,
  webCrawlerReducer,
  yasqeReducer,
  sparqlReducer
});

export default rootReducer;
